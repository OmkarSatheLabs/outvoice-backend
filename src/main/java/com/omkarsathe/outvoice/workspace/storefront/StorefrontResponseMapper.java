package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.storage.ObjectStorageService;
import com.omkarsathe.outvoice.workspace.product.Product;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicProductDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicStorefrontDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontDomainResponse;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StorefrontResponseMapper {

    private final StorefrontUrlService storefrontUrlService;
    private final ObjectStorageService objectStorageService;

    public StorefrontResponse toResponse(Storefront storefront) {
        if (storefront == null) {
            return null;
        }

        List<StorefrontDomainResponse> domainResponses = storefront.getDomains() != null
                ? storefront.getDomains().stream().map(this::toDomainResponse).toList()
                : Collections.emptyList();

        return new StorefrontResponse(
                storefront.getId(),
                storefront.getWorkspace() != null ? storefront.getWorkspace().getId() : null,
                storefront.getSlug(),
                storefront.getName(),
                storefront.getDescription(),
                storefront.getLogoUrl(),
                storefront.getUrlStrategy(),
                storefront.getStatus(),
                storefront.getBrandingConfig(),
                storefront.getPagesConfig(),
                storefrontUrlService.generatePublicUrl(storefront),
                domainResponses,
                storefront.getCreatedAt(),
                storefront.getUpdatedAt()
        );
    }

    public StorefrontDomainResponse toDomainResponse(StorefrontDomain domain) {
        if (domain == null) {
            return null;
        }

        return new StorefrontDomainResponse(
                domain.getId(),
                domain.getHostname(),
                domain.getVerificationStatus(),
                domain.getVerificationToken(),
                "TXT",
                "_outvoice-challenge." + domain.getHostname(),
                domain.getVerificationToken(),
                domain.getVerifiedAt(),
                domain.getTlsStatus(),
                domain.isPrimary(),
                domain.getCreatedAt()
        );
    }

    public PublicStorefrontDto toPublicDto(Storefront storefront) {
        if (storefront == null) {
            return null;
        }

        return new PublicStorefrontDto(
                storefront.getId(),
                storefront.getSlug(),
                storefront.getName(),
                storefront.getDescription(),
                storefront.getLogoUrl(),
                storefront.getUrlStrategy(),
                storefrontUrlService.generatePublicUrl(storefront),
                storefront.getBrandingConfig(),
                storefront.getPagesConfig()
        );
    }

    public PublicProductDto toPublicProductDto(Product product) {
        if (product == null) {
            return null;
        }

        String imageUrl = product.getImageKey() != null && !product.getImageKey().isBlank()
                ? objectStorageService.getUrl(product.getImageKey())
                : product.getImageUrl();

        return new PublicProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getUnit(),
                imageUrl,
                product.getStock() > 0
        );
    }
}
