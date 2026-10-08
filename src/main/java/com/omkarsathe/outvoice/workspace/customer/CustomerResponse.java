package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.phone.PhoneCodeResponse;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String fullName,
        String email,
        PhoneCodeResponse phoneCode,
        String mobile,
        List<CustomerInvoiceResponse> invoices,
        BigDecimal totalInvoiced,
        BigDecimal totalCollected,
        BigDecimal outstandingBalance
) {}
