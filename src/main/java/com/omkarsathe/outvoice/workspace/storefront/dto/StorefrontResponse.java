package com.omkarsathe.outvoice.workspace.storefront.dto;

import com.omkarsathe.outvoice.workspace.storefront.StorefrontStatus;
import com.omkarsathe.outvoice.workspace.storefront.UrlStrategy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StorefrontResponse(
        UUID id,
        UUID workspaceId,
        String slug,
        String name,
        String description,
        String logoUrl,
        UrlStrategy urlStrategy,
        StorefrontStatus status,
        String brandingConfig,
        String pagesConfig,
        String publicUrl,
        List<StorefrontDomainResponse> domains,
        Instant createdAt,
        Instant updatedAt
) {
}
