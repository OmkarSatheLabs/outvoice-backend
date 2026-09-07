package com.omkarsathe.outvoice.pdf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoicePdfData(
        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        WorkspacePdfData workspace,
        CustomerPdfData customer,
        List<InvoiceItemPdfData> items,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal tax,
        BigDecimal total
) {
}
