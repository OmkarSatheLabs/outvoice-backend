package com.omkarsathe.outvoice.workspace.invoice.dto;

import com.omkarsathe.outvoice.workspace.invoice.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PublicInvoiceDto(
        UUID id,
        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal total,
        BigDecimal tax,
        BigDecimal discount,
        BigDecimal netTotal,
        String currencyCode,
        String currencySymbol,
        InvoiceStatus status,
        boolean hasPdf,

        // Biller / Workspace info
        UUID workspaceId,
        String workspaceName,
        String workspaceLogoUrl,
        String workspaceBillingAddress,
        String taxComplianceName,
        String gstNumber,
        String panNumber,

        // Customer info
        String customerName,
        String customerEmail,
        String customerPhone,
        String customerBillingAddress,

        // Items
        List<PublicInvoiceItemDto> items,

        // Payment capabilities
        boolean paymentEnabled,
        String primaryPaymentMethod,
        List<PaymentMethodDto> paymentMethods,
        String razorpayKeyId
) {
}
