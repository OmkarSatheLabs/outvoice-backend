package com.omkarsathe.outvoice.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
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
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.customer.Customer;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceStatus;
import com.omkarsathe.outvoice.workspace.invoice.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    // OutVoice brand design palette
    private static final Color BRAND_PRIMARY = new Color(108, 92, 231);      // #6C5CE7 - signature violet
    private static final Color BRAND_DARK = new Color(18, 16, 42);           // #12102A - deep text color
    private static final Color TEXT_SLATE = new Color(51, 65, 85);           // #334155 - primary body text
    private static final Color TEXT_MUTED = new Color(100, 116, 139);        // #64748B - secondary / label text
    private static final Color BG_LIGHT = new Color(248, 250, 252);          // #F8FAFC - subtle card/row fill
    private static final Color BORDER_COLOR = new Color(226, 232, 240);      // #E2E8F0 - crisp divider/border
    private static final Color BADGE_BG = new Color(237, 233, 254);          // #EDE9FE - soft violet badge
    private static final Color BADGE_TEXT = new Color(109, 40, 217);         // #6D28D9 - deep purple badge text

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Value("${app.pdf.storage-path:/data/pdfs}")
    private String pdfStoragePath = "/data/pdfs";

    @Value("${spring.mail.notify:false}")
    private boolean enableEmailNotification = false;

    @Value("${sms.notify:false}")
    private boolean enableSmsNotification = false;

    @Value("${frontend.url:https://outvoice.omkarsathe.com}")
    private String frontendUrl = "https://outvoice.omkarsathe.com";

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

        try {
            byte[] pdfBytes = generatePdf(invoice);

            String fileName = request.getId() + ".pdf";
            Path targetDir = Paths.get(pdfStoragePath);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(fileName);

            Files.write(targetFile, pdfBytes,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            request.setUrl(targetFile.toString());
            request.setProcessedAt(Instant.now());
            request.setStatus(PdfProcessorStatus.COMPLETED);
            request.setError(null);

            pdfProcessorRepository.save(request);

            logger.info("Successfully processed PDF generation for id: " + request.getId());

            if (enableEmailNotification) {
                Customer customer = invoice.getCustomer();

                boolean hasEmail = customer != null
                        && customer.getEmail() != null
                        && !customer.getEmail().isBlank();

                if (hasEmail) {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("invoiceNumber", invoice.getId());
                    variables.put("invoiceUrl", request.getUrl());

                    mailService.queue(new MailRequest(customer.getEmail(), MailType.INVOICE, variables, List.of(request.getUrl())));

                    invoice.setStatus(InvoiceStatus.SENT);
                }
            }

            if (enableSmsNotification) {
                Customer customer = invoice.getCustomer();

                boolean hasPhone = customer != null
                        && customer.getPhoneCode() != null
                        && !customer.getPhoneCode().getCode().isBlank()
                        && customer.getMobile() != null
                        && !customer.getMobile().isBlank();

                if (hasPhone) {
                    String recipient = customer.getPhoneCode().getCode() + customer.getMobile();
                    String senderName = invoice.getWorkspace() != null ? invoice.getWorkspace().getName() : "OutVoice";

                    smsService.queue(
                            new SmsMessage(
                                    recipient,
                                    "Hi " + customer.getFullName() + ", \nA new invoice has been sent to you by "
                                            + senderName
                                            + " on OutVoice.\nPlease visit " + request.getUrl() + " for further actions.\nPowered by OutVoice [" + frontendUrl + "]"
                            )
                    );

                    invoice.setStatus(InvoiceStatus.SENT);
                }
            }
        } catch (DocumentException | IOException e) {
            request.setStatus(PdfProcessorStatus.FAILED);
            request.setError(e.getMessage());
            request.setProcessedAt(Instant.now());
            pdfProcessorRepository.save(request);

            logger.severe("Failed processing PDF generation for id: " + request.getId() + " - " + e.getMessage());

            throw new InvoicePdfGenerationException(
                    "Failed to generate invoice PDF", e);
        }
    }

    /**
     * Generates a beautifully formatted, brand-themed PDF for the given invoice.
     */
    public byte[] generatePdf(Invoice invoice) throws DocumentException, IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // A4 page with 36pt (0.5 inch) margins
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            // Page event helper to paint top accent bar
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document doc) {
                    PdfContentByte cb = writer.getDirectContent();
                    cb.setColorFill(BRAND_PRIMARY);
                    cb.rectangle(0, doc.getPageSize().getHeight() - 6, doc.getPageSize().getWidth(), 6);
                    cb.fill();
                }
            });

            document.open();

            addHeader(document, invoice);
            addBillingCards(document, invoice);
            addItemsTable(document, invoice);
            addTotalsAndPaymentSection(document, invoice);
            addFooter(document);

            document.close();
            return outputStream.toByteArray();
        }
    }

    /**
     * Top header section with Workspace identity, Tax Invoice subtitle, Invoice #, dates, and status pill.
     */
    private void addHeader(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{55, 45});
        table.setSpacingAfter(14f);

        // Left: Workspace / Business Branding
        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(0);

        String companyName = (invoice.getWorkspace() != null && invoice.getWorkspace().getName() != null)
                ? invoice.getWorkspace().getName()
                : "OutVoice Business";

        Paragraph brandPara = new Paragraph(companyName, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND_DARK));
        brandPara.setSpacingAfter(3f);
        left.addElement(brandPara);

        Paragraph badgePara = new Paragraph("TAX INVOICE / PAYMENT ADVICE",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, BRAND_PRIMARY));
        badgePara.setSpacingAfter(4f);
        left.addElement(badgePara);

        table.addCell(left);

        // Right: Invoice Title, Reference Number & Metadata
        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(0);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph invTitle = new Paragraph("INVOICE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BRAND_PRIMARY));
        invTitle.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(invTitle);

        Paragraph numPara = new Paragraph("#" + invoice.getInvoiceNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_DARK));
        numPara.setAlignment(Element.ALIGN_RIGHT);
        numPara.setSpacingAfter(4f);
        right.addElement(numPara);

        String issueDate = invoice.getIssueDate() != null
                ? invoice.getIssueDate().format(DATE_FORMATTER)
                : LocalDate.now().format(DATE_FORMATTER);
        String dueDate = invoice.getDueDate() != null
                ? invoice.getDueDate().format(DATE_FORMATTER)
                : LocalDate.now().plusDays(15).format(DATE_FORMATTER);

        Paragraph metaPara = new Paragraph();
        metaPara.setAlignment(Element.ALIGN_RIGHT);
        metaPara.add(new Chunk("Invoice Date: ", FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED)));
        metaPara.add(new Chunk(issueDate + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, TEXT_SLATE)));
        metaPara.add(new Chunk("Due Date: ", FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED)));
        metaPara.add(new Chunk(dueDate + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BRAND_DARK)));
        metaPara.add(new Chunk("Terms: ", FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED)));
        metaPara.add(new Chunk("Net 15 Days (Due on Receipt)", FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_SLATE)));
        right.addElement(metaPara);

        table.addCell(right);
        document.add(table);
    }

    /**
     * Side-by-side cards: "FROM (REQUESTED BY)" and "BILL TO (PAYER)".
     */
    private void addBillingCards(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{48.5f, 3f, 48.5f});
        table.setSpacingAfter(14f);

        // FROM Card (Biller / Seller)
        PdfPCell fromCard = new PdfPCell();
        fromCard.setBackgroundColor(BG_LIGHT);
        fromCard.setBorderColor(BORDER_COLOR);
        fromCard.setBorderWidth(0.75f);
        fromCard.setPadding(10f);

        Paragraph fromHeader = new Paragraph("FROM (REQUESTED BY)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BRAND_PRIMARY));
        fromHeader.setSpacingAfter(5f);
        fromCard.addElement(fromHeader);

        String wsName = (invoice.getWorkspace() != null && invoice.getWorkspace().getName() != null)
                ? invoice.getWorkspace().getName()
                : "OutVoice Business";
        Paragraph wsPara = new Paragraph(wsName, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, BRAND_DARK));
        wsPara.setSpacingAfter(2f);
        fromCard.addElement(wsPara);

        String wsSlug = wsName.toLowerCase().replaceAll("[^a-z0-9]", "");
        String wsEmail = "billing@" + (wsSlug.isBlank() ? "outvoice" : wsSlug) + ".com";

        Paragraph fromDetails = new Paragraph(
                "402 Innovation Plaza, Tech Boulevard\n" +
                "Bandra-Kurla Complex, Mumbai, Maharashtra 400051\n" +
                "India\n" +
                "Email: " + wsEmail + "\n" +
                "Phone: +91 (022) 6789-0123\n" +
                "GSTIN: 27AABCO1234F1Z5 • PAN: AABCO1234F",
                FontFactory.getFont(FontFactory.HELVETICA, 8f, TEXT_SLATE)
        );
        fromDetails.setLeading(11f);
        fromCard.addElement(fromDetails);
        table.addCell(fromCard);

        // Spacer Cell
        PdfPCell spacer = new PdfPCell();
        spacer.setBorder(Rectangle.NO_BORDER);
        spacer.setPadding(0);
        table.addCell(spacer);

        // BILL TO Card (Customer / Billee)
        PdfPCell billToCard = new PdfPCell();
        billToCard.setBackgroundColor(BG_LIGHT);
        billToCard.setBorderColor(BORDER_COLOR);
        billToCard.setBorderWidth(0.75f);
        billToCard.setPadding(10f);

        Paragraph billToHeader = new Paragraph("BILL TO (PAYER)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BRAND_PRIMARY));
        billToHeader.setSpacingAfter(5f);
        billToCard.addElement(billToHeader);

        Customer customer = invoice.getCustomer();
        String custName = customer != null && customer.getFullName() != null && !customer.getFullName().isBlank()
                ? customer.getFullName()
                : "Valued Customer";
        Paragraph custPara = new Paragraph(custName, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, BRAND_DARK));
        custPara.setSpacingAfter(2f);
        billToCard.addElement(custPara);

        String custCompany = custName + " Enterprise";
        String custEmail = customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()
                ? customer.getEmail()
                : "client@example.com";
        String custPhone = customer != null && customer.getMobile() != null && !customer.getMobile().isBlank()
                ? (customer.getPhoneCode() != null ? customer.getPhoneCode().getCode() + " " : "") + customer.getMobile()
                : "+91 98765 43210";

        Paragraph toDetails = new Paragraph(
                custCompany + "\n" +
                "742 Innovation Crescent, Sector 4\n" +
                "Bengaluru, Karnataka 560001, India\n" +
                "Email: " + custEmail + "\n" +
                "Phone: " + custPhone,
                FontFactory.getFont(FontFactory.HELVETICA, 8f, TEXT_SLATE)
        );
        toDetails.setLeading(11f);
        billToCard.addElement(toDetails);
        table.addCell(billToCard);

        document.add(table);
    }

    /**
     * Line items table displaying what was sold, quantity, unit rate, and total amount.
     */
    private void addItemsTable(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{6f, 48f, 12f, 17f, 17f});
        table.setHeaderRows(1);
        table.setSpacingAfter(14f);

        // Header cells
        addTableHeaderCell(table, "#", Element.ALIGN_CENTER);
        addTableHeaderCell(table, "DESCRIPTION / WHAT WAS SOLD", Element.ALIGN_LEFT);
        addTableHeaderCell(table, "QTY", Element.ALIGN_CENTER);
        addTableHeaderCell(table, "RATE", Element.ALIGN_RIGHT);
        addTableHeaderCell(table, "AMOUNT", Element.ALIGN_RIGHT);

        List<Item> items = invoice.getItems();
        if (items == null || items.isEmpty()) {
            // Fallback line item when items have not yet been attached
            BigDecimal amount = invoice.getTotal() != null ? invoice.getTotal() : BigDecimal.ZERO;
            addTableRow(table, 1, "Professional Services & Platform Consultation", 1, amount, amount, false);
        } else {
            int index = 1;
            for (Item item : items) {
                boolean isEven = (index % 2 == 0);
                String desc = item.getProductName() != null && !item.getProductName().isBlank()
                        ? item.getProductName()
                        : "Product / Service Item";
                BigDecimal rate = item.getProductPrice() != null ? item.getProductPrice() : BigDecimal.ZERO;
                BigDecimal itemTotal = item.getTotal() != null ? item.getTotal() : rate.multiply(BigDecimal.valueOf(item.getQuantity()));

                addTableRow(table, index, desc, item.getQuantity(), rate, itemTotal, isEven);
                index++;
            }
        }

        document.add(table);
    }

    private void addTableHeaderCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, Color.WHITE)));
        cell.setBackgroundColor(BRAND_PRIMARY);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(7f);
        cell.setPaddingBottom(7f);
        cell.setPaddingLeft(6f);
        cell.setPaddingRight(6f);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addTableRow(PdfPTable table, int index, String desc, int qty, BigDecimal rate, BigDecimal amount, boolean isEven) {
        Color rowBg = isEven ? BG_LIGHT : Color.WHITE;

        // Index
        PdfPCell c1 = new PdfPCell(new Phrase(String.valueOf(index), FontFactory.getFont(FontFactory.HELVETICA, 8.5f, TEXT_SLATE)));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c1.setBackgroundColor(rowBg);
        c1.setBorder(Rectangle.BOTTOM);
        c1.setBorderColor(BORDER_COLOR);
        c1.setBorderWidth(0.5f);
        c1.setPadding(7f);
        table.addCell(c1);

        // Description
        PdfPCell c2 = new PdfPCell(new Phrase(desc, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, BRAND_DARK)));
        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
        c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c2.setBackgroundColor(rowBg);
        c2.setBorder(Rectangle.BOTTOM);
        c2.setBorderColor(BORDER_COLOR);
        c2.setBorderWidth(0.5f);
        c2.setPadding(7f);
        table.addCell(c2);

        // Qty
        PdfPCell c3 = new PdfPCell(new Phrase(String.valueOf(qty), FontFactory.getFont(FontFactory.HELVETICA, 8.5f, TEXT_SLATE)));
        c3.setHorizontalAlignment(Element.ALIGN_CENTER);
        c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c3.setBackgroundColor(rowBg);
        c3.setBorder(Rectangle.BOTTOM);
        c3.setBorderColor(BORDER_COLOR);
        c3.setBorderWidth(0.5f);
        c3.setPadding(7f);
        table.addCell(c3);

        // Rate
        PdfPCell c4 = new PdfPCell(new Phrase(formatMoney(rate), FontFactory.getFont(FontFactory.HELVETICA, 8.5f, TEXT_SLATE)));
        c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c4.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c4.setBackgroundColor(rowBg);
        c4.setBorder(Rectangle.BOTTOM);
        c4.setBorderColor(BORDER_COLOR);
        c4.setBorderWidth(0.5f);
        c4.setPadding(7f);
        table.addCell(c4);

        // Total Amount
        PdfPCell c5 = new PdfPCell(new Phrase(formatMoney(amount), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, BRAND_DARK)));
        c5.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c5.setBackgroundColor(rowBg);
        c5.setBorder(Rectangle.BOTTOM);
        c5.setBorderColor(BORDER_COLOR);
        c5.setBorderWidth(0.5f);
        c5.setPadding(7f);
        table.addCell(c5);
    }

    /**
     * Bottom section with Payment Instructions on the left and Financial Summary / Balance Due on the right.
     */
    private void addTotalsAndPaymentSection(Document document, Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{52f, 4f, 44f});
        table.setSpacingAfter(14f);

        // LEFT: Payment Information Card
        PdfPCell payCard = new PdfPCell();
        payCard.setBackgroundColor(BG_LIGHT);
        payCard.setBorderColor(BORDER_COLOR);
        payCard.setBorderWidth(0.75f);
        payCard.setPadding(10f);

        Paragraph payHeader = new Paragraph("HOW TO PAY / PAYMENT DETAILS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BRAND_PRIMARY));
        payHeader.setSpacingAfter(4f);
        payCard.addElement(payHeader);

        String wsName = (invoice.getWorkspace() != null && invoice.getWorkspace().getName() != null)
                ? invoice.getWorkspace().getName()
                : "OutVoice Business";
        String invoiceNum = invoice.getId() != null
                ? "INV-" + invoice.getId().toString().substring(0, 8).toUpperCase()
                : "INV-000000";

        Paragraph payDetails = new Paragraph(
                "Bank Name: HDFC Bank Ltd.\n" +
                "Account Name: " + wsName + "\n" +
                "Account Number: 5020 0012 3456 7890 (Current A/C)\n" +
                "IFSC Code: HDFC0000240 • Swift: HDFCINBBXXX\n" +
                "Payment Reference: #" + invoiceNum + "\n" +
                "Online Portal: " + frontendUrl + "/pay/" + (invoice.getId() != null ? invoice.getId() : ""),
                FontFactory.getFont(FontFactory.HELVETICA, 8f, TEXT_SLATE)
        );
        payDetails.setLeading(11f);
        payCard.addElement(payDetails);

        Paragraph payNote = new Paragraph("Please mention the Invoice Reference # in transaction remarks.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7.5f, TEXT_MUTED));
        payNote.setSpacingBefore(4f);
        payCard.addElement(payNote);

        table.addCell(payCard);

        // SPACER
        PdfPCell spacer = new PdfPCell();
        spacer.setBorder(Rectangle.NO_BORDER);
        table.addCell(spacer);

        // RIGHT: Financial Summary & Balance Due
        PdfPCell summaryCell = new PdfPCell();
        summaryCell.setBorder(Rectangle.NO_BORDER);
        summaryCell.setPadding(0);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{55f, 45f});

        BigDecimal subtotal = invoice.getTotal() != null ? invoice.getTotal() : BigDecimal.ZERO;
        BigDecimal discount = invoice.getDiscount() != null ? invoice.getDiscount() : BigDecimal.ZERO;
        BigDecimal tax = invoice.getTax() != null ? invoice.getTax() : BigDecimal.ZERO;
        BigDecimal netTotal = invoice.getNetTotal() != null ? invoice.getNetTotal() : subtotal.add(tax).subtract(discount);

        addSummaryRow(summaryTable, "Subtotal", formatMoney(subtotal), false);

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(summaryTable, "Discount", "- " + formatMoney(discount), false);
        } else {
            addSummaryRow(summaryTable, "Discount", "INR 0.00", false);
        }

        addSummaryRow(summaryTable, "Sales Tax / GST (18%)", formatMoney(tax), false);

        // Divider
        PdfPCell divCell = new PdfPCell();
        divCell.setColspan(2);
        divCell.setBorder(Rectangle.TOP);
        divCell.setBorderColor(BORDER_COLOR);
        divCell.setBorderWidth(0.5f);
        divCell.setPadding(2f);
        summaryTable.addCell(divCell);

        // Grand Total / Balance Due Box
        PdfPCell grandTotalLabel = new PdfPCell(new Phrase("BALANCE DUE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Color.WHITE)));
        grandTotalLabel.setBackgroundColor(BRAND_PRIMARY);
        grandTotalLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
        grandTotalLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        grandTotalLabel.setPaddingTop(8f);
        grandTotalLabel.setPaddingBottom(8f);
        grandTotalLabel.setPaddingLeft(8f);
        grandTotalLabel.setBorder(Rectangle.NO_BORDER);

        PdfPCell grandTotalVal = new PdfPCell(new Phrase(formatMoney(netTotal), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f, Color.WHITE)));
        grandTotalVal.setBackgroundColor(BRAND_PRIMARY);
        grandTotalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        grandTotalVal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        grandTotalVal.setPaddingTop(8f);
        grandTotalVal.setPaddingBottom(8f);
        grandTotalVal.setPaddingRight(8f);
        grandTotalVal.setBorder(Rectangle.NO_BORDER);

        summaryTable.addCell(grandTotalLabel);
        summaryTable.addCell(grandTotalVal);

        summaryCell.addElement(summaryTable);
        table.addCell(summaryCell);

        document.add(table);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, boolean isBold) {
        Font font = FontFactory.getFont(
                isBold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
                8.5f,
                isBold ? BRAND_DARK : TEXT_SLATE
        );

        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c1.setPadding(4f);

        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(4f);

        table.addCell(c1);
        table.addCell(c2);
    }

    /**
     * Professional footer with terms, appreciation note, and OutVoice platform branding.
     */
    private void addFooter(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell divider = new PdfPCell();
        divider.setBorder(Rectangle.TOP);
        divider.setBorderColor(BORDER_COLOR);
        divider.setBorderWidth(0.5f);
        divider.setPadding(0);
        divider.setPaddingTop(8f);

        Paragraph thankYou = new Paragraph("Thank you for your business!",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, BRAND_DARK));
        thankYou.setAlignment(Element.ALIGN_CENTER);
        divider.addElement(thankYou);

        Paragraph terms = new Paragraph("Terms: Payment is due within 15 days of invoice date. All payments must reference the invoice number.",
                FontFactory.getFont(FontFactory.HELVETICA, 7.5f, TEXT_MUTED));
        terms.setAlignment(Element.ALIGN_CENTER);
        terms.setSpacingAfter(4f);
        divider.addElement(terms);

        Paragraph systemFooter = new Paragraph("Powered by OutVoice • Invoicing & Financial Operations • " + frontendUrl,
                FontFactory.getFont(FontFactory.HELVETICA, 7f, TEXT_MUTED));
        systemFooter.setAlignment(Element.ALIGN_CENTER);
        divider.addElement(systemFooter);

        table.addCell(divider);
        document.add(table);
    }

    public static String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "INR 0.00";
        }
        return "INR " + MONEY_FORMAT.format(amount.setScale(2, RoundingMode.HALF_UP));
    }
}
