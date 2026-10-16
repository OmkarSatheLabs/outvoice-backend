package com.omkarsathe.outvoice.workspace.invoice.dto;

public record NotifyOfflinePaymentResponse(
        boolean success,
        String message,
        String transactionReference
) {
}
