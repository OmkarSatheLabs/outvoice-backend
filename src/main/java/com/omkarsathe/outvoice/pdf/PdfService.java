package com.omkarsathe.outvoice.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.omkarsathe.outvoice.common.exception.InvoicePdfGenerationException;
import com.omkarsathe.outvoice.mail.MailRequest;
import com.omkarsathe.outvoice.mail.MailService;
import com.omkarsathe.outvoice.mail.MailType;
import com.omkarsathe.outvoice.pdf.processor.PdfProcessor;
import com.omkarsathe.outvoice.pdf.processor.PdfProcessorRepository;
import com.omkarsathe.outvoice.pdf.processor.PdfProcessorStatus;
import com.omkarsathe.outvoice.pdf.processor.PdfType;
import com.omkarsathe.outvoice.sms.SmsMessage;
import com.omkarsathe.outvoice.sms.SmsService;
import com.omkarsathe.outvoice.workspace.customer.Customer;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import com.omkarsathe.outvoice.workspace.invoice.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final Logger logger = Logger.getLogger(PdfService.class.getName());
    private final PdfProcessorRepository pdfProcessorRepository;
    private final ObjectMapper objectMapper;
    private final InvoiceRepository invoiceRepository;
    private final MailService mailService;
    private final SmsService smsService;

    @Value("${app.pdf.storage-path:/data/pdfs}")
    private String pdfStoragePath;

    @Value("${spring.mail.notify:false}")
    private boolean enableEmailNotification;

    @Value("${sms.notify:false}")
    private boolean enableSmsNotification;

    @Value("${frontend.url:https://outvoice.omkarsathe.com}")
    private String frontendUrl;


    public void queue(UUID id, PdfType type) {

        logger.info("Queueing " + type.toString().toLowerCase() + " PDF generation for id: " + id);

        ObjectNode data = objectMapper.createObjectNode();
        data.put("invoiceId", id.toString());

        PdfProcessor processor = PdfProcessor.builder()
                .type(type)
                .data(data)
                .status(PdfProcessorStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        PdfProcessor saved = pdfProcessorRepository.save(processor);

        logger.info("Queued " + type.toString().toLowerCase() + " PDF generation for id: " + id + " with id: " + saved.getId());
    }

    @Transactional
    public void process(PdfProcessor request) {

        logger.info("Processing PDF generation for id: " + request.getId());

        UUID invoiceId = UUID.fromString(request.getData().get("invoiceId").asText());

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoicePdfGenerationException("Invoice not found: " + invoiceId));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 40, 40, 40, 40);

            PdfWriter.getInstance(document, outputStream);

            document.open();

            addHeader(document, invoice);
            addCustomerDetails(document, invoice);
            addItems(document, invoice);
            addTotals(document, invoice);
            addFooter(document);

            document.close();

            String fileName = request.getId() + ".pdf";
            Path targetDir = Paths.get(pdfStoragePath);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(fileName);

            Files.write(targetFile, outputStream.toByteArray(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            request.setUrl(targetFile.toString());
            request.setProcessedAt(Instant.now());
            request.setStatus(PdfProcessorStatus.COMPLETED);
            request.setError(null);

            pdfProcessorRepository.save(request);

            logger.info("Successfully processed PDF generation for id: " + request.getId());

            if (enableEmailNotification) {
                Customer customer = invoice.getCustomer();

                boolean hasEmail = customer.getEmail() != null
                        && !customer.getEmail().isBlank();

                if (hasEmail) {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("invoiceNumber", invoice.getId());
                    variables.put("invoiceUrl", request.getUrl());

                    mailService.queue(new MailRequest(customer.getEmail(), MailType.INVOICE, variables, List.of(request.getUrl())));
                }
            }

            if (enableSmsNotification) {
                Customer customer = invoice.getCustomer();

                boolean hasPhone = customer.getPhoneCode() != null
                        && !customer.getPhoneCode().getCode().isBlank()
                        && customer.getMobile() != null
                        && !customer.getMobile().isBlank();

                if (hasPhone) {

                    String recipient = customer.getPhoneCode().getCode() + customer.getMobile();

                    smsService.queue(
                            new SmsMessage(
                                    recipient,
                                    "Hi " + customer.getFullName() + ", \nA new invoice has been sent to you by "
                                            + invoice.getWorkspace().getName()
                                            + " on OutVoice.\nPlease visit " + request.getUrl() + " for further actions.\nPowered by OutVoice [" + frontendUrl + "]"
                            )
                    );
                }
            }
        } catch (DocumentException | IOException e) {
            request.setStatus(PdfProcessorStatus.FAILED);
            request.setError(e.getMessage());
            request.setProcessedAt(Instant.now());
            pdfProcessorRepository.save(request);

            logger.info("Failed processing PDF generation for id: " + request.getId());

            throw new InvoicePdfGenerationException(
                    "Failed to generate invoice PDF", e);
        }
    }

    private void addHeader(Document document, Invoice invoice)
            throws DocumentException {

        Font brandFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                22,
                Color.BLACK
        );

        Font invoiceFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                18,
                Color.BLACK
        );

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{70, 30});

        PdfPCell brand = new PdfPCell();
        brand.setBorder(Rectangle.NO_BORDER);

        Paragraph workspaceName =
                new Paragraph(invoice.getWorkspace().getName(), brandFont);

        brand.addElement(workspaceName);

        PdfPCell invoiceTitle = new PdfPCell();
        invoiceTitle.setBorder(Rectangle.NO_BORDER);
        invoiceTitle.setHorizontalAlignment(Element.ALIGN_RIGHT);

        invoiceTitle.addElement(
                new Paragraph("INVOICE", invoiceFont)
        );

        table.addCell(brand);
        table.addCell(invoiceTitle);

        document.add(table);

        document.add(Chunk.NEWLINE);
    }

    private void addCustomerDetails(
            Document document,
            Invoice invoice
    ) throws DocumentException {

        Font sectionFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                10
        );

        Font normalFont = FontFactory.getFont(
                FontFactory.HELVETICA,
                10
        );

        Paragraph title = new Paragraph("BILL TO", sectionFont);

        document.add(title);

        Customer customer = invoice.getCustomer();

        String name = customer.getFullName();

        document.add(new Paragraph(name, normalFont));

        if (customer.getEmail() != null) {
            document.add(
                    new Paragraph(customer.getEmail(), normalFont)
            );
        }

        if (customer.getMobile() != null) {
            document.add(
                    new Paragraph(
                            customer.getPhoneCode().getCode()
                                    + " "
                                    + customer.getMobile(),
                            normalFont
                    )
            );
        }

        document.add(Chunk.NEWLINE);
    }

    private void addItems(
            Document document,
            Invoice invoice
    ) throws DocumentException {

        Font headerFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                9
        );

        Font bodyFont = FontFactory.getFont(
                FontFactory.HELVETICA,
                9
        );

        PdfPTable table = new PdfPTable(4);

        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 15, 17, 18});

        addHeaderCell(table, "Description", headerFont);
        addHeaderCell(table, "Qty", headerFont);
        addHeaderCell(table, "Price", headerFont);
        addHeaderCell(table, "Total", headerFont);

        for (Item item : invoice.getItems()) {

            addBodyCell(
                    table,
                    item.getProductName(),
                    bodyFont
            );

            addBodyCell(
                    table,
                    String.valueOf(item.getQuantity()),
                    bodyFont
            );

            addBodyCell(
                    table,
                    formatMoney(item.getProductPrice()),
                    bodyFont
            );

            addBodyCell(
                    table,
                    formatMoney(item.getTotal()),
                    bodyFont
            );
        }

        document.add(table);

        document.add(Chunk.NEWLINE);
    }

    private void addHeaderCell(
            PdfPTable table,
            String text,
            Font font
    ) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text, font)
        );

        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);

        table.addCell(cell);
    }

    private void addBodyCell(
            PdfPTable table,
            String text,
            Font font
    ) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text, font)
        );

        cell.setPadding(8);
        cell.setBorder(Rectangle.BOTTOM);

        table.addCell(cell);
    }

    private String formatMoney(BigDecimal amount) {
        return "₹" + amount.setScale(
                2,
                RoundingMode.HALF_UP
        ).toPlainString();
    }

    private void addTotals(
            Document document,
            Invoice invoice
    ) throws DocumentException {

        PdfPTable table = new PdfPTable(2);

        table.setWidthPercentage(45);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addTotalRow(
                table,
                "Subtotal",
                formatMoney(invoice.getTotal()),
                false
        );

        addTotalRow(
                table,
                "Discount",
                formatMoney(invoice.getDiscount()),
                false
        );

        addTotalRow(
                table,
                "Tax",
                formatMoney(invoice.getTax()),
                false
        );

        addTotalRow(
                table,
                "Total",
                formatMoney(invoice.getNetTotal()),
                true
        );

        document.add(table);
    }

    private void addTotalRow(
            PdfPTable table,
            String label,
            String value,
            boolean emphasized
    ) {
        Font font = FontFactory.getFont(
                FontFactory.HELVETICA,
                emphasized ? Font.BOLD : Font.NORMAL,
                emphasized ? 11 : 10
        );

        PdfPCell labelCell = new PdfPCell(
                new Phrase(label, font)
        );
        labelCell.setBorder(
                emphasized ? Rectangle.TOP : Rectangle.NO_BORDER
        );
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(
                new Phrase(value, font)
        );
        valueCell.setBorder(
                emphasized ? Rectangle.TOP : Rectangle.NO_BORDER
        );
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document)
            throws DocumentException {

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        Font footerFont = FontFactory.getFont(
                FontFactory.HELVETICA,
                8,
                Color.GRAY
        );

        Paragraph footer = new Paragraph(
                "Thank you for your business.",
                footerFont
        );

        footer.setAlignment(Element.ALIGN_CENTER);

        document.add(footer);
    }
}
