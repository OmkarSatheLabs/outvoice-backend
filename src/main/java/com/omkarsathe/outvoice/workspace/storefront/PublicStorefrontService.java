package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.product.Product;
import com.omkarsathe.outvoice.workspace.product.ProductRepository;
import com.omkarsathe.outvoice.workspace.product.ProductStatus;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicProductDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicStorefrontDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicStorefrontService {

    private final StorefrontRepository storefrontRepository;
    private final StorefrontDomainRepository storefrontDomainRepository;
    private final ProductRepository productRepository;
    private final StorefrontResponseMapper storefrontResponseMapper;
    private final StorefrontUrlService storefrontUrlService;

    @Transactional(readOnly = true)
    public PublicStorefrontDto resolveBySlug(String slug) {
        String normalizedSlug = ReservedSlugs.normalizeSlug(slug);
        Storefront storefront = storefrontRepository.findBySlugIgnoreCase(normalizedSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found: " + slug));

        if (storefront.getStatus() != StorefrontStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Storefront is currently unavailable");
        }

        return storefrontResponseMapper.toPublicDto(storefront);
    }

    @Transactional(readOnly = true)
    public PublicStorefrontDto resolveByHost(String host) {
        String normalizedHost = ReservedSlugs.normalizeHostname(host);

        // 1. Check custom domain mapping
        var domainOpt = storefrontDomainRepository.findByHostnameIgnoreCase(normalizedHost);
        if (domainOpt.isPresent()) {
            StorefrontDomain domain = domainOpt.get();
            if (domain.getVerificationStatus() == DomainVerificationStatus.VERIFIED) {
                Storefront storefront = domain.getStorefront();
                if (storefront.getStatus() == StorefrontStatus.PUBLISHED) {
                    return storefrontResponseMapper.toPublicDto(storefront);
                }
            }
        }

        // 2. Check OutVoice subdomain
        String subdomainSlug = storefrontUrlService.extractSubdomainSlugFromHost(normalizedHost);
        if (subdomainSlug != null && !subdomainSlug.isBlank()) {
            return resolveBySlug(subdomainSlug);
        }

        throw new ResourceNotFoundException("No active storefront configured for host: " + host);
    }

    @Transactional(readOnly = true)
    public PublicStorefrontDto resolve(String host, String slug) {
        if (slug != null && !slug.isBlank()) {
            return resolveBySlug(slug);
        }
        if (host != null && !host.isBlank()) {
            return resolveByHost(host);
        }
        throw new ResourceNotFoundException("Storefront slug or host parameter must be provided");
    }

    @Transactional(readOnly = true)
    public List<PublicProductDto> getPublishedProducts(UUID storefrontId) {
        Storefront storefront = storefrontRepository.findById(storefrontId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found: " + storefrontId));

        if (storefront.getStatus() != StorefrontStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Storefront is currently unavailable");
        }

        UUID workspaceId = storefront.getWorkspace().getId();
        return productRepository.findAllByWorkspace_IdAndStatusAndDeletedAtIsNull(workspaceId, ProductStatus.PUBLISHED)
                .stream()
                .map(storefrontResponseMapper::toPublicProductDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicProductDto getPublishedProduct(UUID storefrontId, UUID productId) {
        Storefront storefront = storefrontRepository.findById(storefrontId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found: " + storefrontId));

        if (storefront.getStatus() != StorefrontStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Storefront is currently unavailable");
        }

        UUID workspaceId = storefront.getWorkspace().getId();
        Product product = productRepository.findByIdAndWorkspace_IdAndStatusAndDeletedAtIsNull(productId, workspaceId, ProductStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        return storefrontResponseMapper.toPublicProductDto(product);
    }
}
