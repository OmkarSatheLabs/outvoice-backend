package com.omkarsathe.outvoice.pdf;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.customer.Customer;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdfServiceTest {

    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService(null, null, null, null, null);
    }

    @Test
    void testGeneratePdfWithAllInvoiceParameters() throws Exception {
        UUID invoiceId = UUID.randomUUID();

        Workspace workspace = Workspace.builder()
                .id(UUID.randomUUID())
                .name("HyperGrowth Technologies")
                .build();

        PhoneCode phoneCode = PhoneCode.builder()
                .code("+91")
                .build();

        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .workspace(workspace)
                .fullName("Aarav Mehta")
                .email("aarav.mehta@acmecorp.com")
                .phoneCode(phoneCode)
                .mobile("9876543210")
                .build();

        List<Item> items = new ArrayList<>();
        items.add(Item.builder()
                .productName("SaaS Enterprise Subscription (1 Year)")
                .productPrice(new BigDecimal("15000.00"))
                .quantity(1)
                .total(new BigDecimal("15000.00"))
                .build());
        items.add(Item.builder()
                .productName("Cloud Infrastructure Migration & Setup")
                .productPrice(new BigDecimal("3500.00"))
                .quantity(2)
                .total(new BigDecimal("7000.00"))
                .build());
        items.add(Item.builder()
                .productName("Dedicated Premium Support (24x7)")
                .productPrice(new BigDecimal("2000.00"))
                .quantity(1)
                .total(new BigDecimal("2000.00"))
                .build());

        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .workspace(workspace)
                .customer(customer)
                .total(new BigDecimal("24000.00"))
                .tax(new BigDecimal("4320.00"))
                .discount(new BigDecimal("1000.00"))
                .netTotal(new BigDecimal("27320.00"))
                .issueDate(LocalDate.of(2026, 9, 1))
                .dueDate(LocalDate.of(2026, 9, 16))
                .items(items)
                .build();

        byte[] pdfBytes = pdfService.generatePdf(invoice);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        PdfReader reader = new PdfReader(pdfBytes);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        String text = extractor.getTextFromPage(1);

        // Verify From
        assertTrue(text.contains("HyperGrowth Technologies"));
        assertTrue(text.contains("FROM (REQUESTED BY)"));
        assertTrue(text.contains("Mumbai, Maharashtra"));
        assertTrue(text.contains("GSTIN:"));

        // Verify Bill To
        assertTrue(text.contains("BILL TO (PAYER)"));
        assertTrue(text.contains("Aarav Mehta"));
        assertTrue(text.contains("aarav.mehta@acmecorp.com"));
        assertTrue(text.contains("+91 9876543210"));

        // Verify Due Dates & Terms
        String shortId = invoiceId.toString().substring(0, 8).toUpperCase();
        assertTrue(text.contains("INV-" + shortId));
        assertTrue(text.contains("Sep 01, 2026"));
        assertTrue(text.contains("Sep 16, 2026"));
        assertTrue(text.contains("Net 15 Days"));

        // Verify Sold Items
        assertTrue(text.contains("SaaS Enterprise Subscription"));
        assertTrue(text.contains("Cloud Infrastructure Migration"));
        assertTrue(text.contains("Dedicated Premium Support"));

        // Verify Totals
        assertTrue(text.contains("Subtotal"));
        assertTrue(text.contains("INR 24,000.00"));
        assertTrue(text.contains("Discount"));
        assertTrue(text.contains("- INR 1,000.00"));
        assertTrue(text.contains("Sales Tax / GST (18%)"));
        assertTrue(text.contains("INR 4,320.00"));
        assertTrue(text.contains("BALANCE DUE"));
        assertTrue(text.contains("INR 27,320.00"));

        // Verify Payment instructions
        assertTrue(text.contains("HOW TO PAY / PAYMENT DETAILS"));
        assertTrue(text.contains("HDFC Bank Ltd."));
        assertTrue(text.contains("5020 0012 3456 7890"));
        assertTrue(text.contains("HDFC0000240"));
        assertTrue(text.contains("#INV-" + shortId));

        // Verify Footer
        assertTrue(text.contains("Thank you for your business!"));
        assertTrue(text.contains("Powered by OutVoice"));
    }

    @Test
    void testGeneratePdfWithEmptyItemsFallback() throws Exception {
        Invoice invoice = Invoice.builder()
                .id(UUID.randomUUID())
                .workspace(Workspace.builder().name("Solo Founder").build())
                .customer(Customer.builder().fullName("Jane Doe").build())
                .total(new BigDecimal("500.00"))
                .tax(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .netTotal(new BigDecimal("500.00"))
                .items(new ArrayList<>())
                .build();

        byte[] pdfBytes = pdfService.generatePdf(invoice);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        PdfReader reader = new PdfReader(pdfBytes);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        String text = extractor.getTextFromPage(1);

        assertTrue(text.contains("Solo Founder"));
        assertTrue(text.contains("Jane Doe"));
        assertTrue(text.contains("BALANCE DUE"));
        assertTrue(text.contains("INR 500.00"));
    }
}
