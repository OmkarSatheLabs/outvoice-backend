package com.omkarsathe.outvoice.workspace.payment.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentGatewayConfigResponse(
        UUID id,
        UUID workspaceId,
        String provider,
        boolean isEnabled,
        boolean isPrimary,

        // Razorpay
        String keyId,
        String maskedKeySecret,
        boolean hasWebhookSecret,
        String webhookUrl,

        // UPI
        String upiId,
        String payeeName,

        // Bank Transfer
        String bankAccountName,
        String bankAccountNumber,
        String bankIfscCode,
        String bankName,
        String bankBranch,
        String bankSwiftCode,
        String instructions,

        Instant updatedAt
) {
}
