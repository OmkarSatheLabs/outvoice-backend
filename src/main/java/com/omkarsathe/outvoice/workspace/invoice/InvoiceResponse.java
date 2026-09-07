package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.workspace.WorkspaceResponse;
import com.omkarsathe.outvoice.workspace.customer.CustomerResponse;
import com.omkarsathe.outvoice.workspace.invoice.item.ItemResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        WorkspaceResponse workspace,
        CustomerResponse customer,
        BigDecimal total,
        BigDecimal tax,
        BigDecimal discount,
        BigDecimal netTotal,
        List<ItemResponse> items
) {
}
