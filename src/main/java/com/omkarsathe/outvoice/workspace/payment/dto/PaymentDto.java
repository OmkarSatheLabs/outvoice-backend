package com.omkarsathe.outvoice.workspace.payment.dto;

import com.omkarsathe.outvoice.workspace.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentDto(
        UUID id,
        UUID invoiceId,
        BigDecimal amount,
        PaymentStatus status,
        String provider,
        String orderId,
        String providerPaymentId,
        String paymentMethod,
        String notes,
        Instant completedAt,
        Instant createdAt
) {
}
