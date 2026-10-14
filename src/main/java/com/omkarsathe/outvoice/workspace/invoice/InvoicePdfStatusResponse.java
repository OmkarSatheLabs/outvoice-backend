package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.pdf.processor.PdfProcessorStatus;

public record InvoicePdfStatusResponse(
        PdfProcessorStatus status,
        String pdfUrl,
        String error
) {
}
