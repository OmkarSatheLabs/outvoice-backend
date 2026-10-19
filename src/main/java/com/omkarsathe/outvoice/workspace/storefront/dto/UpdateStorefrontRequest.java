package com.omkarsathe.outvoice.workspace.storefront.dto;

import com.omkarsathe.outvoice.workspace.storefront.UrlStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateStorefrontRequest(
        @NotBlank(message = "Storefront slug is required")
        @Size(min = 3, max = 63, message = "Slug must be between 3 and 63 characters")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug must contain only lowercase alphanumeric characters and hyphens, and cannot start or end with a hyphen")
        String slug,

        @NotBlank(message = "Storefront name is required")
        @Size(max = 255, message = "Storefront name must be at most 255 characters")
        String name,

        String description,

        String logoUrl,

        UrlStrategy urlStrategy,

        String brandingConfig,

        String pagesConfig
) {
}
