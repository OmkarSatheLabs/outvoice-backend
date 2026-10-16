package com.omkarsathe.outvoice.workspace.invoice.dto;

public record RazorpayPaymentVerifyResponse(
        boolean success,
        String message,
        String paymentId,
        String invoiceStatus
) {
}
