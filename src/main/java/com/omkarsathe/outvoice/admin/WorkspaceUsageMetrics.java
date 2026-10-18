package com.omkarsathe.outvoice.admin;

import java.math.BigDecimal;

public record WorkspaceUsageMetrics(
        int members,
        int invoices,
        BigDecimal billed
) {
}
