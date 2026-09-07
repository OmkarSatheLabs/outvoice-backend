package com.omkarsathe.outvoice.pdf;

import java.math.BigDecimal;

public record InvoiceItemPdfData(
        String name,
        BigDecimal unitPrice,
        BigDecimal quantity,
        BigDecimal total
) {
}
