package com.omkarsathe.outvoice.workspace.storefront.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicProductDto(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        String unit,
        String imageUrl,
        boolean inStock
) {
}
