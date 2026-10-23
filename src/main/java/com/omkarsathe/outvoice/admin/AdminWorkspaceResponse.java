package com.omkarsathe.outvoice.admin;

import com.omkarsathe.outvoice.workspace.WorkspaceStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminWorkspaceResponse(
        UUID id,
        String name,
        String invoiceNumberPrefix,
        WorkspaceStatus status,
        boolean suspended,
        Instant createdAt,
        WorkspaceUsageMetrics metrics
) {
}
