package com.omkarsathe.outvoice.workspace.invoice.dto;

import jakarta.validation.constraints.NotBlank;

public record RazorpayPaymentVerifyRequest(
        @NotBlank(message = "razorpay_order_id is required")
        String razorpayOrderId,

        @NotBlank(message = "razorpay_payment_id is required")
        String razorpayPaymentId,

        @NotBlank(message = "razorpay_signature is required")
        String razorpaySignature
) {
}
