package com.omkarsathe.outvoice.workspace.product;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final WorkspaceRepository workspaceRepository;
    private final ProductRepository productRepository;
    private final ProductResponseMapper productResponseMapper;
    private final AuditService auditService;
    private final ProductImageStorageService productImageStorageService;

    @Transactional
    public ProductResponse create(UUID workspaceId, CreateProduct request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workspace not found: " + workspaceId
                        ));

        int stock = request.stock() != null ? request.stock() : 0;
        String unit = (request.unit() != null && !request.unit().isBlank()) ? request.unit().trim() : null;

        Product product = Product.builder()
                .workspace(workspace)
                .name(request.name().trim())
                .stock(stock)
                .unit(unit)
                .price(request.price())
                .build();

        Product saved = productRepository.save(product);

        auditService.log(
                workspaceId,
                AuditAction.CREATE,
                AuditResourceType.PRODUCT,
                saved.getId(),
                "Created product " + saved.getName(),
                Map.of(
                        "name", saved.getName() != null ? saved.getName() : "",
                        "price", saved.getPrice() != null ? saved.getPrice() : BigDecimal.ZERO,
                        "stock", saved.getStock(),
                        "unit", saved.getUnit() != null ? saved.getUnit() : ""
                )
        );

        return productResponseMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(UUID workspaceId, UUID productId, UpdateProduct request) {
        Product product = productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        int stock = request.stock() != null ? request.stock() : 0;
        String unit = (request.unit() != null && !request.unit().isBlank()) ? request.unit().trim() : null;

        product.setName(request.name().trim());
        product.setPrice(request.price());
        product.setStock(stock);
        product.setUnit(unit);

        Product saved = productRepository.save(product);

        auditService.log(
                workspaceId,
                AuditAction.UPDATE,
                AuditResourceType.PRODUCT,
                saved.getId(),
                "Updated product " + saved.getName(),
                Map.of(
                        "name", saved.getName() != null ? saved.getName() : "",
                        "price", saved.getPrice() != null ? saved.getPrice() : BigDecimal.ZERO,
                        "stock", saved.getStock(),
                        "unit", saved.getUnit() != null ? saved.getUnit() : ""
                )
        );

        return productResponseMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID workspaceId, UUID productId) {
        Product product = productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        product.setDeletedAt(Instant.now());
        productRepository.save(product);

        auditService.log(
                workspaceId,
                AuditAction.DELETE,
                AuditResourceType.PRODUCT,
                product.getId(),
                "Deleted product " + product.getName(),
                Map.of(
                        "name", product.getName() != null ? product.getName() : "",
                        "deletedAt", product.getDeletedAt().toString()
                )
        );
    }

    @Transactional
    public ProductResponse uploadImage(UUID workspaceId, UUID productId, MultipartFile file) {
        Product product = productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        String relativePathOrUrl = productImageStorageService.storeImage(productId, file);
        product.setImageUrl(relativePathOrUrl);
        Product saved = productRepository.save(product);

        auditService.log(
                workspaceId,
                AuditAction.UPDATE,
                AuditResourceType.PRODUCT,
                saved.getId(),
                "Updated image for product " + saved.getName(),
                Map.of(
                        "name", saved.getName() != null ? saved.getName() : "",
                        "imageUrl", saved.getImageUrl() != null ? saved.getImageUrl() : ""
                )
        );

        return productResponseMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse removeImage(UUID workspaceId, UUID productId) {
        Product product = productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        productImageStorageService.deleteImage(productId);
        product.setImageUrl(null);
        Product saved = productRepository.save(product);

        auditService.log(
                workspaceId,
                AuditAction.UPDATE,
                AuditResourceType.PRODUCT,
                saved.getId(),
                "Removed image for product " + saved.getName(),
                Map.of(
                        "name", saved.getName() != null ? saved.getName() : ""
                )
        );

        return productResponseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getProductImage(UUID productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Resource resource = productImageStorageService.loadImageAsResource(productId);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.IMAGE_JPEG);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(resource);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(UUID workspaceId) {
        return productRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId)
                .stream()
                .map(productResponseMapper::toResponse)
                .toList();
    }
}
