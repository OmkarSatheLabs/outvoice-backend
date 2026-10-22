package com.omkarsathe.outvoice.workspace.storefront.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record StorefrontCheckoutRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Valid email is required")
        String customerEmail,

        String customerMobile,
        String customerAddress,
        String notes,

        @NotEmpty(message = "Cart cannot be empty")
        @Valid
        List<CartItemRequest> items
) {
    public record CartItemRequest(
            @NotNull(message = "Product ID is required")
            UUID productId,

            @Positive(message = "Quantity must be greater than zero")
            int quantity
    ) {}
}
