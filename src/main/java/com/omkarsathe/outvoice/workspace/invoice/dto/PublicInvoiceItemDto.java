package com.omkarsathe.outvoice.workspace.invoice.dto;

import java.math.BigDecimal;

public record PublicInvoiceItemDto(
        String productName,
        BigDecimal productPrice,
        int quantity,
        BigDecimal total
) {
}
