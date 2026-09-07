package com.omkarsathe.outvoice.workspace.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {
    private String invoiceNumber;
    private UUID customerId;
    private BigDecimal amount;
    private String currencyCode;
    private String status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal taxRate;
    private List<LineItemDto> lineItems;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItemDto {
        private Integer sortOrder;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal taxRate;
    }
}
