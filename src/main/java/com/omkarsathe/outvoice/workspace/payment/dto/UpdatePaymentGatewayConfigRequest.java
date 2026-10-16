package com.omkarsathe.outvoice.workspace.payment.dto;

public record UpdatePaymentGatewayConfigRequest(
        boolean isEnabled,
        boolean isPrimary,

        // Razorpay
        String keyId,
        String keySecret,
        String webhookSecret,
        boolean testConnection,

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
        String instructions
) {
}
