package com.omkarsathe.outvoice.workspace.invoice.dto;

public record PaymentMethodDto(
        String provider,
        boolean isPrimary,

        // Razorpay
        String razorpayKeyId,

        // UPI
        String upiId,
        String payeeName,
        String upiUri,

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
