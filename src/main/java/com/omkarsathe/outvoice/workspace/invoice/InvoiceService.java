package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.common.exception.ResourceNotEditableException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.mail.MailRequest;
import com.omkarsathe.outvoice.mail.MailService;
import com.omkarsathe.outvoice.mail.MailType;
import com.omkarsathe.outvoice.pdf.PdfService;
import com.omkarsathe.outvoice.pdf.processor.InvoicePdfProcessorData;
import com.omkarsathe.outvoice.pdf.processor.InvoicePdfProcessorHandler;
import com.omkarsathe.outvoice.pdf.processor.PdfType;
import com.omkarsathe.outvoice.sms.SmsMessage;
import com.omkarsathe.outvoice.sms.SmsService;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.customer.Customer;
import com.omkarsathe.outvoice.workspace.customer.CustomerRepository;
import com.omkarsathe.outvoice.workspace.invoice.item.CreateItem;
import com.omkarsathe.outvoice.workspace.invoice.item.Item;
import com.omkarsathe.outvoice.workspace.product.Product;
import com.omkarsathe.outvoice.workspace.product.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final Logger logger = Logger.getLogger(InvoiceService.class.getName());
    private final InvoiceRepository invoiceRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;
    private final ProductRepository productRepository;
    private final SmsService smsService;
    private final MailService mailService;
    private final PdfService pdfService;
    private final InvoicePdfProcessorHandler invoicePdfProcessorHandler;

    @Transactional
    public InvoiceResponse create(UUID workspaceId, CreateInvoice request) {

        logger.info("Creating Invoice for Workspace " + workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workspace not found: " + workspaceId
                        ));

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + request.customerId()
                        ));

        BigDecimal total = BigDecimal.ZERO;

        Invoice invoice = Invoice.builder()
                .workspace(workspace)
                .customer(customer)
                .invoiceNumber(request.isDraft() ? null : generateInvoiceNumber(workspace.getInvoiceNumberPrefix(), workspace.getNextInvoiceSequence()))
                .tax(request.tax())
                .discount(request.discount())
                .issueDate(request.issueDate())
                .dueDate(request.dueDate())
                .status(request.isDraft() ? InvoiceStatus.DRAFT : InvoiceStatus.CREATED)
                .items(new ArrayList<>())
                .build();

        for (CreateItem requestItem : request.items()) {

            logger.info("Creating invoice item for new Invoice");

            Product product = (requestItem.productId() != null)
            ? productRepository.findById(requestItem.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + requestItem.productId()))
                    : null;

            Integer quantity = requestItem.quantity();

            BigDecimal productPrice = (product != null)
                    ? product.getPrice()
                    : requestItem.productPrice();

            String productName = (product != null)
                    ? product.getName()
                    : requestItem.productName();

            BigDecimal itemTotal = productPrice.multiply(BigDecimal.valueOf(quantity));

            Item item = Item.builder()
                    .invoice(invoice)
                    .product(product)
                    .productName(productName)
                    .productPrice(productPrice)
                    .quantity(quantity)
                    .total(itemTotal)
                    .build();

            total = total.add(itemTotal);

            invoice.getItems().add(item);
        }

        invoice.setTotal(total);
        invoice.setNetTotal((total.add(request.tax())).subtract(request.discount()));

        invoice = invoiceRepository.save(invoice);

        logger.info("Success " + invoice.getStatus() + " Invoice: " + invoice.getId());

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            pdfService.queue(invoice.getId(), PdfType.INVOICE);

            workspace.setNextInvoiceSequence(workspace.getNextInvoiceSequence() + 1);
        }

        boolean hasEmail = customer.getEmail() != null
                && !customer.getEmail().isBlank();

        boolean hasPhone = customer.getPhoneCode() != null
                && !customer.getPhoneCode().getCode().isBlank()
                && customer.getMobile() != null
                && !customer.getMobile().isBlank();

//        if (hasEmail) {
//            Map<String, Object> variables = new HashMap<>();
//            variables.put("invoiceNumber", invoice.getId());
//
//            mailService.queue(new MailRequest(customer.getEmail(), MailType.INVOICE, variables));
//        }
//
//        if (hasPhone) {
//
//            String recipient = customer.getPhoneCode().getCode() + customer.getMobile();
//
//            smsService.queue(
//                    new SmsMessage(
//                            recipient,
//                            "Hi " + customer.getFullName() + ", \nA new invoice has been sent to you by "
//                                    + workspace.getName()
//                                    + " on OutVoice.\nVisit us at https://outvoice.omkarsathe.com"
//                    )
//            );
//        }

        return invoiceResponseMapper.toResponse(invoice);
    }

    private String generateInvoiceNumber(String invoiceNumberPrefix, Long nextInvoiceSequence) {
        return String.format("%s-%08d", invoiceNumberPrefix, nextInvoiceSequence);
    }

    @Transactional
    public List<InvoiceResponse> getInvoices(UUID workspaceId) {
        return invoiceRepository.findByWorkspace_IdAndDeletedAtIsNull(workspaceId)
                .stream()
                .map(invoiceResponseMapper::toResponse)
                .toList();
    }

    @Transactional
    public InvoiceResponse getInvoice(UUID workspaceId, UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        return invoiceResponseMapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceSummaryResponse getSummary(UUID workspaceId) {
        List<Invoice> invoices = invoiceRepository.findByWorkspace_IdAndDeletedAtIsNull(workspaceId);

        BigDecimal total = BigDecimal.ZERO;

        for (Invoice invoice : invoices) {
            total = total.add(invoice.getNetTotal());
        }

        return new InvoiceSummaryResponse(total, total, total, total);
    }

    @Transactional
    public InvoiceResponse update(UUID workspaceId, UUID invoiceId, UpdateInvoice request) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workspace not found: " + workspaceId
                        ));

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice not found: " + invoiceId
                        ));

        if (!invoice.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException(
                    "Invoice not found: " + invoiceId
            );
        }

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ResourceNotEditableException(
                    "Invoice " + invoiceId + " is not in draft"
            );
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + request.customerId()
                        ));

        if (!customer.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException(
                    "Customer not found: " + request.customerId()
            );
        }

        invoice.setCustomer(customer);
        invoice.setInvoiceNumber(request.isDraft() ? null : generateInvoiceNumber(workspace.getInvoiceNumberPrefix(), workspace.getNextInvoiceSequence()));
        invoice.setIssueDate(request.issueDate());
        invoice.setDueDate(request.dueDate());
        invoice.setTax(request.tax());
        invoice.setDiscount(request.discount());
        invoice.setUpdated_at(Instant.now());
        invoice.setStatus(request.isDraft() ? InvoiceStatus.DRAFT : InvoiceStatus.CREATED);

        invoice.getItems().clear();

        BigDecimal total = BigDecimal.ZERO;

        for (CreateItem requestItem : request.items()) {

            logger.info("Creating invoice item for new Invoice");

            Product product = (requestItem.productId() != null)
                    ? productRepository.findById(requestItem.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + requestItem.productId()))
                    : null;

            Integer quantity = requestItem.quantity();

            BigDecimal productPrice = (product != null)
                    ? product.getPrice()
                    : requestItem.productPrice();

            String productName = (product != null)
                    ? product.getName()
                    : requestItem.productName();

            BigDecimal itemTotal = productPrice.multiply(BigDecimal.valueOf(quantity));

            Item item = Item.builder()
                    .invoice(invoice)
                    .product(product)
                    .productName(productName)
                    .productPrice(productPrice)
                    .quantity(quantity)
                    .total(itemTotal)
                    .build();

            total = total.add(itemTotal);

            invoice.getItems().add(item);
        }

        invoice.setTotal(total);
        invoice.setNetTotal((total.add(request.tax())).subtract(request.discount()));

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        logger.info("Success " + updatedInvoice.getStatus() + " Invoice: " + updatedInvoice.getId());

        if (updatedInvoice.getStatus() != InvoiceStatus.DRAFT) {
            pdfService.queue(updatedInvoice.getId(), PdfType.INVOICE);

            workspace.setNextInvoiceSequence(workspace.getNextInvoiceSequence() + 1);
        }

        return invoiceResponseMapper.toResponse(updatedInvoice);
    }

    @Transactional
    public ResponseEntity<?> delete(UUID workspaceId, UUID invoiceId) {

        workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workspace not found: " + workspaceId
                        ));

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice not found: " + invoiceId
                        ));

        if (!invoice.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException(
                    "Invoice not found: " + invoiceId
            );
        }

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ResourceNotEditableException(
                    "Invoice " + invoiceId + " is not in draft"
            );
        }

        invoice.setDeletedAt(Instant.now());

        invoiceRepository.save(invoice);

        return new ResponseEntity<>(HttpStatus.OK);
    }

//    @Transactional
//    public ResponseEntity<byte[]> sendInvoice(UUID invoiceId) {
//        Invoice invoice = invoiceRepository.findById(invoiceId)
//                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
//
////        byte[] pdf = pdfService.generate(invoice.getId());
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_PDF)
//                .header(
//                        HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"invoice-" +
//                                invoiceId +
//                                ".pdf\""
//                )
//                .body(pdf);
//    }
}
