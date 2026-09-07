package com.omkarsathe.outvoice.workspace.invoice.item;

import com.omkarsathe.outvoice.workspace.product.ProductResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemResponse(
        UUID id,
        ProductResponse product,
        String productName,
        BigDecimal productPrice,
        int quantity,
        BigDecimal total
) {
}
