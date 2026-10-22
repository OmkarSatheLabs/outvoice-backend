package com.omkarsathe.outvoice.workspace.storefront.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StorefrontCheckoutResponse(
        UUID invoiceId,
        String invoiceNumber,
        String invoiceAccessToken,
        BigDecimal totalAmount,
        String customerName,
        String customerEmail,
        String message
) {}
