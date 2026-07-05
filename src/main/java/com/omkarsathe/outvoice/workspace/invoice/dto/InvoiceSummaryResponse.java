package com.omkarsathe.outvoice.workspace.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSummaryResponse {
    private BigDecimal totalInvoiced;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
    private BigDecimal totalOverdue;
    private String currencyCode;
    private Map<String, Long> countByStatus;
}
