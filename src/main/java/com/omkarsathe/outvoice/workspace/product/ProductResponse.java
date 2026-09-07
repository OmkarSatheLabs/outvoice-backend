package com.omkarsathe.outvoice.workspace.product;

import com.omkarsathe.outvoice.workspace.WorkspaceResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        WorkspaceResponse workspace,
        String name,
        int stock,
        String unit,
        BigDecimal price
) {
}
