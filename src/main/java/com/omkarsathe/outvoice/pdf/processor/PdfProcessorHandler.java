package com.omkarsathe.outvoice.pdf.processor;

public interface PdfProcessorHandler {

    PdfType getType();

    void queue(PdfType type, InvoicePdfProcessorData data);
}
