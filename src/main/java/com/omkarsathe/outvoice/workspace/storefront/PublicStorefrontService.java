package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.customer.Customer;
import com.omkarsathe.outvoice.workspace.customer.CustomerRepository;
import com.omkarsathe.outvoice.workspace.invoice.CreateInvoice;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceAccessTokenService;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceResponse;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceService;
import com.omkarsathe.outvoice.workspace.invoice.item.CreateItem;
import com.omkarsathe.outvoice.workspace.product.Product;
import com.omkarsathe.outvoice.workspace.product.ProductRepository;
import com.omkarsathe.outvoice.workspace.product.ProductStatus;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicProductDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicStorefrontDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontCheckoutRequest;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontCheckoutResponse;
import com.omkarsathe.outvoice.workspace.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private final CustomerRepository customerRepository;
    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceAccessTokenService tokenService;

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

    @Transactional
    public StorefrontCheckoutResponse checkout(UUID storefrontId, StorefrontCheckoutRequest request) {
        Storefront storefront = storefrontRepository.findById(storefrontId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found: " + storefrontId));

        if (storefront.getStatus() != StorefrontStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Storefront is currently unavailable for orders");
        }

        Workspace workspace = storefront.getWorkspace();
        UUID workspaceId = workspace.getId();

        // 1. Find or create customer
        String email = request.customerEmail().trim().toLowerCase();
        Customer customer = customerRepository
                .findByWorkspace_IdAndEmailIgnoreCaseAndDeletedAtIsNull(workspaceId, email)
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .workspace(workspace)
                            .fullName(request.customerName().trim())
                            .email(email)
                            .mobile(request.customerMobile() != null && !request.customerMobile().isBlank() ? request.customerMobile().trim() : null)
                            .address(request.customerAddress() != null && !request.customerAddress().isBlank() ? request.customerAddress().trim() : null)
                            .build();
                    return customerRepository.save(newCustomer);
                });

        // 2. Validate product items
        List<CreateItem> invoiceItems = new ArrayList<>();
        for (StorefrontCheckoutRequest.CartItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

            if (!product.getWorkspace().getId().equals(workspaceId) || product.getStatus() != ProductStatus.PUBLISHED) {
                throw new ResourceNotFoundException("Product not available in this storefront: " + itemReq.productId());
            }

            invoiceItems.add(new CreateItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    itemReq.quantity()
            ));
        }

        // 3. Create invoice with issue date = today, due date = 7 days from now
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(7);
        CreateInvoice createInvoice = new CreateInvoice(
                customer.getId(),
                issueDate,
                dueDate,
                BigDecimal.ZERO, // Tax
                BigDecimal.ZERO, // Discount
                invoiceItems,
                false // Not draft -> created invoice
        );

        InvoiceResponse invoiceResponse = invoiceService.createInvoice(workspaceId, createInvoice);

        // 4. Generate public access token
        Invoice invoice = invoiceRepository.findById(invoiceResponse.id())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceResponse.id()));
        var token = tokenService.getOrCreateToken(invoice);

        return new StorefrontCheckoutResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                token.getTokenHash(),
                invoice.getNetTotal(),
                customer.getFullName(),
                customer.getEmail(),
                "Your order has been placed successfully!"
        );
    }
}
