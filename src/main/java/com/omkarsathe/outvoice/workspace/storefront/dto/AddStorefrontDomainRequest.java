package com.omkarsathe.outvoice.workspace.storefront.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddStorefrontDomainRequest(
        @NotBlank(message = "Hostname is required")
        @Pattern(
                regexp = "^(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.[A-Za-z0-9-]{1,63})*\\.[A-Za-z]{2,}$",
                message = "Invalid hostname format (e.g. store.acme.com)"
        )
        String hostname
) {
}
