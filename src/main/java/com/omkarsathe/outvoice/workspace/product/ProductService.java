package com.omkarsathe.outvoice.workspace.product;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
import com.omkarsathe.outvoice.common.exception.ForbiddenException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.storage.ObjectStorageService;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import com.omkarsathe.outvoice.storage.model.UploadRequest;
import com.omkarsathe.outvoice.storage.validation.ImageValidator;
import com.omkarsathe.outvoice.storage.validation.ValidatedImage;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
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
@Slf4j
public class ProductService {

    private final WorkspaceRepository workspaceRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final MemberRepository memberRepository;
    private final ProductResponseMapper productResponseMapper;
    private final AuditService auditService;
    private final ObjectStorageService objectStorageService;
    private final ImageValidator imageValidator;

    @Transactional
    public ProductResponse create(UUID workspaceId, CreateProduct request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workspace not found: " + workspaceId
                        ));

        int stock = request.stock() != null ? request.stock() : 0;
        String unit = (request.unit() != null && !request.unit().isBlank()) ? request.unit().trim() : null;
        ProductStatus status = request.status() != null ? request.status() : ProductStatus.DRAFT;
        String description = (request.description() != null && !request.description().isBlank()) ? request.description().trim() : null;

        Product product = Product.builder()
                .workspace(workspace)
                .name(request.name().trim())
                .stock(stock)
                .unit(unit)
                .price(request.price())
                .status(status)
                .description(description)
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
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        if (request.description() != null) {
            product.setDescription(request.description().trim().isEmpty() ? null : request.description().trim());
        }

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
    public ProductResponse uploadImage(UUID workspaceId, UUID productId, MultipartFile file, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Product product = productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // 1. Content and MIME validation
        ValidatedImage validatedImage = imageValidator.validate(file);

        // 2. Generate scoped, safe object key: workspaces/{workspaceId}/products/{productId}/{imageId}.{ext}
        String imageId = UUID.randomUUID().toString();
        String newObjectKey = "workspaces/" + workspaceId + "/products/" + productId + "/" + imageId + validatedImage.extension();

        // 3. Upload to object storage first
        UploadRequest uploadRequest = new UploadRequest(
                newObjectKey,
                validatedImage.inputStream(),
                validatedImage.contentLength(),
                validatedImage.contentType(),
                Map.of("workspaceId", workspaceId.toString(), "productId", productId.toString()),
                true
        );
        objectStorageService.upload(uploadRequest);

        String oldObjectKey = product.getImageKey();

        // 4. Update database with compensation on failure
        Product saved;
        try {
            // Save ProductImage entity record
            ProductImage productImage = ProductImage.builder()
                    .workspace(product.getWorkspace())
                    .product(product)
                    .objectKey(newObjectKey)
                    .fileName(file.getOriginalFilename())
                    .contentType(validatedImage.contentType())
                    .fileSize(validatedImage.contentLength())
                    .isPrimary(true)
                    .displayOrder(0)
                    .createdAt(Instant.now())
                    .build();
            productImageRepository.save(productImage);

            String publicUrl = objectStorageService.getUrl(newObjectKey);
            product.setImageKey(newObjectKey);
            product.setImageUrl(publicUrl);
            saved = productRepository.save(product);

            auditService.log(
                    workspaceId,
                    AuditAction.UPDATE,
                    AuditResourceType.PRODUCT,
                    saved.getId(),
                    "Updated image for product " + saved.getName(),
                    Map.of(
                            "name", saved.getName() != null ? saved.getName() : "",
                            "imageKey", newObjectKey,
                            "imageUrl", publicUrl != null ? publicUrl : ""
                    )
            );
        } catch (Exception e) {
            // Compensate partial failure: remove newly uploaded file from object storage
            log.error("Database save failed during image upload for product: {}. Compensating by deleting uploaded object: {}",
                    productId, newObjectKey, e);
            try {
                objectStorageService.delete(newObjectKey);
            } catch (Exception cleanupEx) {
                log.warn("Failed to delete newly uploaded object during rollback: {}", newObjectKey, cleanupEx);
            }
            throw e;
        }

        // 5. Clean up old image from storage only after successful DB update
        if (oldObjectKey != null && !oldObjectKey.isBlank() && !oldObjectKey.equals(newObjectKey)) {
            try {
                objectStorageService.delete(oldObjectKey);
                log.debug("Successfully deleted previous product image: {}", oldObjectKey);
            } catch (Exception deleteEx) {
                log.warn("Failed to delete replaced product image from storage: {}", oldObjectKey, deleteEx);
            }
        }

        return productResponseMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse uploadImage(UUID workspaceId, UUID productId, MultipartFile file) {
        return uploadImage(workspaceId, productId, file, null);
    }

    @Transactional
    public ProductResponse removeImage(UUID workspaceId, UUID productId, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Product product = productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        String oldObjectKey = product.getImageKey();

        product.setImageKey(null);
        product.setImageUrl(null);
        productImageRepository.deleteAllByProduct_Id(productId);
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

        if (oldObjectKey != null && !oldObjectKey.isBlank()) {
            try {
                objectStorageService.delete(oldObjectKey);
                log.debug("Successfully deleted product image: {}", oldObjectKey);
            } catch (Exception e) {
                log.warn("Failed to delete removed product image from storage: {}", oldObjectKey, e);
            }
        }

        return productResponseMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse removeImage(UUID workspaceId, UUID productId) {
        return removeImage(workspaceId, productId, null);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getProductImage(UUID productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (product.getImageKey() == null || product.getImageKey().isBlank()) {
            throw new ResourceNotFoundException("Product image not found for product: " + productId);
        }

        StorageObject storageObject = objectStorageService.load(product.getImageKey());
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(storageObject.metadata().contentType());
        } catch (Exception e) {
            mediaType = MediaType.IMAGE_JPEG;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(storageObject.metadata().contentLength())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(new InputStreamResource(storageObject.inputStream()));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(UUID workspaceId) {
        return productRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId)
                .stream()
                .map(productResponseMapper::toResponse)
                .toList();
    }

    private void validateWorkspaceAccess(UUID workspaceId, UUID currentUserId) {
        if (currentUserId != null) {
            boolean isMember = memberRepository.existsByWorkspaceIdAndUserId(workspaceId, currentUserId);
            if (!isMember) {
                throw new ForbiddenException("You are not a member of this workspace", workspaceId.toString());
            }
        }
    }
}
