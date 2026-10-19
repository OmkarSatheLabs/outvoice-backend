package com.omkarsathe.outvoice.workspace.product;

import com.omkarsathe.outvoice.audit.log.AuditService;
import com.omkarsathe.outvoice.common.exception.ForbiddenException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.storage.ObjectStorageService;
import com.omkarsathe.outvoice.storage.model.StorageMetadata;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import com.omkarsathe.outvoice.storage.model.UploadRequest;
import com.omkarsathe.outvoice.storage.validation.ImageValidator;
import com.omkarsathe.outvoice.storage.validation.ValidatedImage;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceStorageTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ProductResponseMapper productResponseMapper;
    @Mock
    private AuditService auditService;
    @Mock
    private ObjectStorageService objectStorageService;
    @Mock
    private ImageValidator imageValidator;

    private ProductService productService;

    private final UUID workspaceId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private Workspace workspace;
    private Product product;

    @BeforeEach
    void setUp() {
        productService = new ProductService(
                workspaceRepository,
                productRepository,
                productImageRepository,
                memberRepository,
                productResponseMapper,
                auditService,
                objectStorageService,
                imageValidator
        );

        workspace = Workspace.builder()
                .id(workspaceId)
                .name("Acme Studio")
                .build();

        product = Product.builder()
                .id(productId)
                .workspace(workspace)
                .name("Standard Widget")
                .price(BigDecimal.valueOf(25.00))
                .stock(50)
                .status(ProductStatus.PUBLISHED)
                .build();
    }

    @Test
    void uploadImage_throwsForbidden_whenUserIsNotWorkspaceMember() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () ->
                productService.uploadImage(workspaceId, productId, file, userId));

        verifyNoInteractions(objectStorageService);
        verifyNoInteractions(imageValidator);
    }

    @Test
    void uploadImage_uploadsNewObjectAndSavesProduct_whenValid() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
        ValidatedImage validatedImage = new ValidatedImage(
                "image/jpeg", ".jpg", 100, 100, 3, new ByteArrayInputStream(new byte[]{1, 2, 3})
        );

        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId))
                .thenReturn(Optional.of(product));
        when(imageValidator.validate(file)).thenReturn(validatedImage);
        when(objectStorageService.getUrl(any())).thenAnswer(inv -> "http://localhost:8080/files/" + inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.uploadImage(workspaceId, productId, file, userId);

        ArgumentCaptor<UploadRequest> uploadCaptor = ArgumentCaptor.forClass(UploadRequest.class);
        verify(objectStorageService).upload(uploadCaptor.capture());
        assertTrue(uploadCaptor.getValue().objectKey().startsWith("workspaces/" + workspaceId + "/products/" + productId + "/"));
        assertTrue(uploadCaptor.getValue().objectKey().endsWith(".jpg"));

        verify(productImageRepository).save(any(ProductImage.class));
        verify(productRepository).save(product);
        assertNotNull(product.getImageKey());
        assertTrue(product.getImageUrl().contains("http://localhost:8080/files/"));
    }

    @Test
    void uploadImage_deletesOldObject_whenReplacingExistingImage() {
        String oldKey = "workspaces/" + workspaceId + "/products/" + productId + "/old-image.png";
        product.setImageKey(oldKey);
        product.setImageUrl("http://localhost:8080/files/" + oldKey);

        MockMultipartFile file = new MockMultipartFile("file", "new.webp", "image/webp", new byte[]{1, 2, 3});
        ValidatedImage validatedImage = new ValidatedImage(
                "image/webp", ".webp", 200, 200, 3, new ByteArrayInputStream(new byte[]{1, 2, 3})
        );

        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId))
                .thenReturn(Optional.of(product));
        when(imageValidator.validate(file)).thenReturn(validatedImage);
        when(objectStorageService.getUrl(any())).thenAnswer(inv -> "http://localhost:8080/files/" + inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.uploadImage(workspaceId, productId, file, userId);

        verify(objectStorageService).upload(any(UploadRequest.class));
        verify(objectStorageService).delete(oldKey);
    }

    @Test
    void uploadImage_compensatesByDeletingNewObject_whenDatabaseSaveFails() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
        ValidatedImage validatedImage = new ValidatedImage(
                "image/jpeg", ".jpg", 100, 100, 3, new ByteArrayInputStream(new byte[]{1, 2, 3})
        );

        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId))
                .thenReturn(Optional.of(product));
        when(imageValidator.validate(file)).thenReturn(validatedImage);
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("DB Outage"));

        assertThrows(RuntimeException.class, () ->
                productService.uploadImage(workspaceId, productId, file, userId));

        ArgumentCaptor<UploadRequest> uploadCaptor = ArgumentCaptor.forClass(UploadRequest.class);
        verify(objectStorageService).upload(uploadCaptor.capture());
        String uploadedKey = uploadCaptor.getValue().objectKey();

        // Must compensate by deleting the uploaded file
        verify(objectStorageService).delete(uploadedKey);
    }

    @Test
    void removeImage_clearsImageAndDeletesFromStorage() {
        String existingKey = "workspaces/" + workspaceId + "/products/" + productId + "/img.jpg";
        product.setImageKey(existingKey);
        product.setImageUrl("http://localhost:8080/files/" + existingKey);

        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(productRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(productId, workspaceId))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.removeImage(workspaceId, productId, userId);

        assertNull(product.getImageKey());
        assertNull(product.getImageUrl());
        verify(productImageRepository).deleteAllByProduct_Id(productId);
        verify(objectStorageService).delete(existingKey);
    }

    @Test
    void getProductImage_loadsFromStorageService() {
        String key = "workspaces/" + workspaceId + "/products/" + productId + "/sample.png";
        product.setImageKey(key);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));

        StorageMetadata metadata = new StorageMetadata(
                key, 128L, "image/png", Instant.now(), "etag", java.util.Map.of()
        );
        StorageObject storageObject = new StorageObject(
                key, new ByteArrayInputStream(new byte[128]), metadata
        );
        when(objectStorageService.load(key)).thenReturn(storageObject);

        ResponseEntity<Resource> response = productService.getProductImage(productId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("image/png", response.getHeaders().getContentType().toString());
    }

    @Test
    void getProductImage_throwsNotFound_whenNoImageKey() {
        product.setImageKey(null);
        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductImage(productId));
    }
}
