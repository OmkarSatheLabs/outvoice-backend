package com.omkarsathe.outvoice.workspace.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordPaymentRequest(
        String provider,
        BigDecimal amount,
        LocalDate paymentDate,
        String transactionReference,
        String notes
) {
}
