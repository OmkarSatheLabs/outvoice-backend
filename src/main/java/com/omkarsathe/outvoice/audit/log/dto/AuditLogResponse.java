package com.omkarsathe.outvoice.audit.log.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID workspaceId,
        String action,
        String resourceType,
        UUID resourceId,
        UUID changedBy,
        String actorName,
        String actorEmail,
        String changeDescription,
        JsonNode changes,
        Instant changedAt
) {
}
