package com.omkarsathe.outvoice.workspace.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateProduct(
        @NotBlank(message = "Name is required")
        String name,

        Integer stock,

        String unit,

        @NotNull(message = "Price cannot be null")
        @Digits(integer = 6, fraction = 2, message = "Price format must be up to 6 integer digits and 2 decimals")
        @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
        @DecimalMax(value = "999999.99", message = "Price cannot exceed 999999.99")
        BigDecimal price,

        ProductStatus status,

        String description
) {
}
