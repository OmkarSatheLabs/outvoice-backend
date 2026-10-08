package com.omkarsathe.outvoice.workspace.invoice;

import java.math.BigDecimal;

public record InvoiceSummaryResponse(
        BigDecimal totalInvoiced,
        BigDecimal totalPaid,
        BigDecimal totalPending,
        BigDecimal totalOverdue
) {
}
