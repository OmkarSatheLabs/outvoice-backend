package com.omkarsathe.outvoice.workspace.invoice.dto;

import jakarta.validation.constraints.NotBlank;

public record NotifyOfflinePaymentRequest(
        @NotBlank(message = "Payment provider is required (UPI or BANK_TRANSFER)")
        String provider,

        @NotBlank(message = "Transaction reference or UTR is required")
        String transactionReference,

        String notes
) {
}
