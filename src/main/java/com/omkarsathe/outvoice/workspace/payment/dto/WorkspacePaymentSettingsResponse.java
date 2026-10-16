package com.omkarsathe.outvoice.workspace.payment.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkspacePaymentSettingsResponse(
        UUID id,
        UUID workspaceId,
        String provider,
        String keyId,
        String maskedKeySecret,
        boolean hasWebhookSecret,
        boolean isEnabled,
        String webhookUrl,
        Instant updatedAt
) {
}
