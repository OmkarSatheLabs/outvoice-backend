package com.omkarsathe.outvoice.pdf;

import com.lowagie.text.DocumentException;
import com.omkarsathe.outvoice.common.exception.InvoicePdfGenerationException;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl {

    private final InvoiceRepository invoiceRepository;
    private final PdfService pdfService;

    public byte[] generate(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoicePdfGenerationException("Invoice not found: " + invoiceId));

        try {
            return pdfService.generatePdf(invoice);
        } catch (DocumentException | IOException e) {
            throw new InvoicePdfGenerationException("Failed to generate invoice PDF", e);
        }
    }
}
