package com.omkarsathe.outvoice.workspace.invoice.dto;

public record CreateRazorpayOrderResponse(
        String orderId,
        String keyId,
        long amount,
        String currency,
        String invoiceNumber,
        String customerName,
        String customerEmail,
        String customerPhone
) {
}
