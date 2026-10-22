package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.product.Product;
import com.omkarsathe.outvoice.workspace.product.ProductRepository;
import com.omkarsathe.outvoice.workspace.product.ProductStatus;
import com.omkarsathe.outvoice.workspace.customer.Customer;
import com.omkarsathe.outvoice.workspace.customer.CustomerRepository;
import com.omkarsathe.outvoice.workspace.invoice.CreateInvoice;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceAccessToken;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceAccessTokenService;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceResponse;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceService;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicProductDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicStorefrontDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontCheckoutRequest;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontCheckoutResponse;
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
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceAccessTokenService tokenService;

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
                storefrontUrlService,
                customerRepository,
                invoiceService,
                invoiceRepository,
                tokenService
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

    @Test
    void checkout_createsInvoiceAndReturnsAccessDetails() {
        when(storefrontRepository.findById(storefrontId)).thenReturn(Optional.of(storefront));

        UUID prodId = UUID.randomUUID();
        Product product = Product.builder()
                .id(prodId)
                .workspace(workspace)
                .name("Ceramic Mug")
                .price(BigDecimal.valueOf(25.00))
                .stock(50)
                .status(ProductStatus.PUBLISHED)
                .build();
        when(productRepository.findById(prodId)).thenReturn(Optional.of(product));

        when(customerRepository.findByWorkspace_IdAndEmailIgnoreCaseAndDeletedAtIsNull(workspaceId, "jane@example.com"))
                .thenReturn(Optional.empty());

        Customer savedCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .workspace(workspace)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .build();
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        UUID invoiceId = UUID.randomUUID();
        InvoiceResponse mockInvoiceResponse = new InvoiceResponse(
                invoiceId, null, null, "INV-001",
                java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(7),
                BigDecimal.valueOf(50.00), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(50.00),
                null, null, null, List.of()
        );
        when(invoiceService.createInvoice(eq(workspaceId), any(CreateInvoice.class)))
                .thenReturn(mockInvoiceResponse);

        Invoice mockInvoice = Invoice.builder()
                .id(invoiceId)
                .invoiceNumber("INV-001")
                .netTotal(BigDecimal.valueOf(50.00))
                .build();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(mockInvoice));

        InvoiceAccessToken mockToken = InvoiceAccessToken.builder()
                .tokenHash("sec_tok_12345")
                .build();
        when(tokenService.getOrCreateToken(mockInvoice)).thenReturn(mockToken);

        StorefrontCheckoutRequest req = new StorefrontCheckoutRequest(
                "Jane Doe",
                "jane@example.com",
                "9876543210",
                "123 Street",
                "Handle with care",
                List.of(new StorefrontCheckoutRequest.CartItemRequest(prodId, 2))
        );

        StorefrontCheckoutResponse response = publicStorefrontService.checkout(storefrontId, req);

        assertNotNull(response);
        assertEquals(invoiceId, response.invoiceId());
        assertEquals("INV-001", response.invoiceNumber());
        assertEquals("sec_tok_12345", response.invoiceAccessToken());
        assertEquals(BigDecimal.valueOf(50.00), response.totalAmount());
        assertEquals("Jane Doe", response.customerName());
        assertEquals("jane@example.com", response.customerEmail());
    }
}
