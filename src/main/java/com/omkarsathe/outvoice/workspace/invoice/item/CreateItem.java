package com.omkarsathe.outvoice.workspace.invoice.item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateItem(
        UUID productId,

        String productName,

        BigDecimal productPrice,

        @NotNull(message = "Quantity must be present")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity
) {
}
