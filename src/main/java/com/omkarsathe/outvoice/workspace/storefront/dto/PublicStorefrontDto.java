package com.omkarsathe.outvoice.workspace.storefront.dto;

import com.omkarsathe.outvoice.workspace.storefront.UrlStrategy;

import java.util.UUID;

public record PublicStorefrontDto(
        UUID id,
        String slug,
        String name,
        String description,
        String logoUrl,
        UrlStrategy urlStrategy,
        String canonicalUrl,
        String brandingConfig,
        String pagesConfig
) {
}
