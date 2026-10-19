package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.product.Product;
import com.omkarsathe.outvoice.workspace.product.ProductRepository;
import com.omkarsathe.outvoice.workspace.product.ProductStatus;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicProductDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicStorefrontDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicStorefrontServiceTest {

    @Mock
    private StorefrontRepository storefrontRepository;
    @Mock
    private StorefrontDomainRepository storefrontDomainRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StorefrontResponseMapper storefrontResponseMapper;
    @Mock
    private StorefrontUrlService storefrontUrlService;

    private PublicStorefrontService publicStorefrontService;

    private final UUID workspaceId = UUID.randomUUID();
    private final UUID storefrontId = UUID.randomUUID();
    private Workspace workspace;
    private Storefront storefront;

    @BeforeEach
    void setUp() {
        publicStorefrontService = new PublicStorefrontService(
                storefrontRepository,
                storefrontDomainRepository,
                productRepository,
                storefrontResponseMapper,
                storefrontUrlService
        );

        workspace = Workspace.builder()
                .id(workspaceId)
                .name("Acme Studio")
                .build();

        storefront = Storefront.builder()
                .id(storefrontId)
                .workspace(workspace)
                .slug("acme-studio")
                .name("Acme Studio")
                .urlStrategy(UrlStrategy.PATH_BASED)
                .status(StorefrontStatus.PUBLISHED)
                .brandingConfig("{}")
                .pagesConfig("{}")
                .build();
    }

    @Test
    void resolveBySlug_returnsStorefront_whenPublished() {
        when(storefrontRepository.findBySlugIgnoreCase("acme-studio")).thenReturn(Optional.of(storefront));

        PublicStorefrontDto mockDto = new PublicStorefrontDto(
                storefrontId, "acme-studio", "Acme Studio", null, null,
                UrlStrategy.PATH_BASED, "https://dev.outvoice.omkarsathe.com/acme-studio", "{}", "{}"
        );
        when(storefrontResponseMapper.toPublicDto(storefront)).thenReturn(mockDto);

        PublicStorefrontDto dto = publicStorefrontService.resolveBySlug("acme-studio");

        assertNotNull(dto);
        assertEquals("acme-studio", dto.slug());
    }

    @Test
    void resolveBySlug_throwsNotFound_whenUnpublished() {
        storefront.setStatus(StorefrontStatus.DRAFT);
        when(storefrontRepository.findBySlugIgnoreCase("acme-studio")).thenReturn(Optional.of(storefront));

        assertThrows(ResourceNotFoundException.class, () ->
                publicStorefrontService.resolveBySlug("acme-studio"));
    }

    @Test
    void resolveByHost_resolvesVerifiedCustomDomain() {
        StorefrontDomain domain = StorefrontDomain.builder()
                .id(UUID.randomUUID())
                .storefront(storefront)
                .hostname("store.acme.com")
                .verificationStatus(DomainVerificationStatus.VERIFIED)
                .tlsStatus(TlsStatus.ACTIVE)
                .build();

        when(storefrontDomainRepository.findByHostnameIgnoreCase("store.acme.com")).thenReturn(Optional.of(domain));

        PublicStorefrontDto mockDto = new PublicStorefrontDto(
                storefrontId, "acme-studio", "Acme Studio", null, null,
                UrlStrategy.CUSTOM_DOMAIN, "https://store.acme.com", "{}", "{}"
        );
        when(storefrontResponseMapper.toPublicDto(storefront)).thenReturn(mockDto);

        PublicStorefrontDto dto = publicStorefrontService.resolveByHost("store.acme.com");

        assertNotNull(dto);
        assertEquals("acme-studio", dto.slug());
    }

    @Test
    void resolveByHost_resolvesOutVoiceSubdomain() {
        when(storefrontDomainRepository.findByHostnameIgnoreCase("acme-studio.dev.outvoice.omkarsathe.com"))
                .thenReturn(Optional.empty());
        when(storefrontUrlService.extractSubdomainSlugFromHost("acme-studio.dev.outvoice.omkarsathe.com"))
                .thenReturn("acme-studio");
        when(storefrontRepository.findBySlugIgnoreCase("acme-studio")).thenReturn(Optional.of(storefront));

        PublicStorefrontDto mockDto = new PublicStorefrontDto(
                storefrontId, "acme-studio", "Acme Studio", null, null,
                UrlStrategy.OUTVOICE_SUBDOMAIN, "https://acme-studio.dev.outvoice.omkarsathe.com", "{}", "{}"
        );
        when(storefrontResponseMapper.toPublicDto(storefront)).thenReturn(mockDto);

        PublicStorefrontDto dto = publicStorefrontService.resolveByHost("acme-studio.dev.outvoice.omkarsathe.com");

        assertNotNull(dto);
        assertEquals("acme-studio", dto.slug());
    }

    @Test
    void getPublishedProducts_returnsOnlyPublishedProducts() {
        when(storefrontRepository.findById(storefrontId)).thenReturn(Optional.of(storefront));

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .workspace(workspace)
                .name("Handcrafted Pottery")
                .price(BigDecimal.valueOf(49.99))
                .stock(10)
                .status(ProductStatus.PUBLISHED)
                .build();

        when(productRepository.findAllByWorkspace_IdAndStatusAndDeletedAtIsNull(workspaceId, ProductStatus.PUBLISHED))
                .thenReturn(List.of(product));

        PublicProductDto mockProdDto = new PublicProductDto(
                product.getId(), product.getName(), null, product.getPrice(),
                product.getStock(), null, null, true
        );
        when(storefrontResponseMapper.toPublicProductDto(product)).thenReturn(mockProdDto);

        List<PublicProductDto> products = publicStorefrontService.getPublishedProducts(storefrontId);

        assertEquals(1, products.size());
        assertEquals("Handcrafted Pottery", products.get(0).name());
    }

    @Test
    void getPublishedProduct_throwsNotFound_whenProductBelongsToAnotherWorkspace() {
        when(storefrontRepository.findById(storefrontId)).thenReturn(Optional.of(storefront));
        UUID otherProductId = UUID.randomUUID();

        when(productRepository.findByIdAndWorkspace_IdAndStatusAndDeletedAtIsNull(otherProductId, workspaceId, ProductStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                publicStorefrontService.getPublishedProduct(storefrontId, otherProductId));
    }
}
