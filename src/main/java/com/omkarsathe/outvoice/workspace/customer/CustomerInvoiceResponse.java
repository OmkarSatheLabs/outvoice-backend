package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.workspace.invoice.InvoiceStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerInvoiceResponse(
        UUID id,
        String invoiceNumber,
        InvoiceStatus status,
        BigDecimal netTotal
) {
}
