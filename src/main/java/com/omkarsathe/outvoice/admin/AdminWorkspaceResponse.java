package com.omkarsathe.outvoice.admin;

import java.util.UUID;

public record AdminWorkspaceResponse(
        UUID id,
        String name,
        WorkspaceUsageMetrics metrics
) {
}
