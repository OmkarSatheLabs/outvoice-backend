package com.omkarsathe.outvoice.workspace.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateWorkspacePaymentSettingsRequest(
        @NotBlank(message = "Razorpay Key ID is required")
        String keyId,

        // Optional if already stored and user only updates toggle or webhook
        String keySecret,

        String webhookSecret,

        boolean isEnabled,

        boolean testConnection
) {
}
