package com.omkarsathe.outvoice.workspace.invoice;

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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
                .tax(request.tax())
                .discount(request.discount())
                .issueDate(request.issueDate())
                .dueDate(request.dueDate())
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

        logger.info("Created Invoice: " + invoice.getId());

        pdfService.queue(invoice.getId(), PdfType.INVOICE);

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

    @Transactional
    public List<InvoiceResponse> getInvoices(UUID workspaceId) {
        return invoiceRepository.findByWorkspace_Id(workspaceId)
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
