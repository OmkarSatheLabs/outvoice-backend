package com.omkarsathe.outvoice.storage.r2;

import com.omkarsathe.outvoice.storage.config.StorageProperties;
import com.omkarsathe.outvoice.storage.exception.StorageException;
import com.omkarsathe.outvoice.storage.exception.StorageFileNotFoundException;
import com.omkarsathe.outvoice.storage.model.StorageMetadata;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import com.omkarsathe.outvoice.storage.model.UploadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudflareR2ObjectStorageServiceTest {

    @Mock
    private S3Client s3Client;

    private CloudflareR2ObjectStorageService storageService;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.getR2().setBucket("outvoice-assets");
        properties.getR2().setPublicBaseUrl("https://files.outvoice.omkarsathe.com/");
        storageService = new CloudflareR2ObjectStorageService(s3Client, properties);
    }

    @Test
    void upload_callsS3ClientPutObjectWithCorrectParameters() {
        String key = "workspaces/w-1/products/p-1/image.webp";
        byte[] data = "binary-data".getBytes(StandardCharsets.UTF_8);

        UploadRequest request = new UploadRequest(
                key,
                new ByteArrayInputStream(data),
                data.length,
                "image/webp",
                Map.of("workspaceId", "w-1"),
                true
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        storageService.upload(request);

        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(putCaptor.capture(), any(RequestBody.class));

        PutObjectRequest captured = putCaptor.getValue();
        assertEquals("outvoice-assets", captured.bucket());
        assertEquals(key, captured.key());
        assertEquals("image/webp", captured.contentType());
        assertEquals(data.length, captured.contentLength());
        assertEquals("w-1", captured.metadata().get("workspaceId"));
    }

    @Test
    void delete_callsS3ClientDeleteObject() {
        String key = "workspaces/w-1/products/p-1/image.webp";

        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        storageService.delete(key);

        ArgumentCaptor<DeleteObjectRequest> delCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(delCaptor.capture());
        assertEquals("outvoice-assets", delCaptor.getValue().bucket());
        assertEquals(key, delCaptor.getValue().key());
    }

    @Test
    void exists_returnsTrueWhenHeadObjectSucceeds() {
        String key = "workspaces/w-1/products/p-1/image.webp";
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        assertTrue(storageService.exists(key));
    }

    @Test
    void exists_returnsFalseWhenNoSuchKeyExceptionThrown() {
        String key = "workspaces/w-1/products/p-1/image.webp";
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("Key not found").build());

        assertFalse(storageService.exists(key));
    }

    @Test
    void getMetadata_returnsMappedStorageMetadata() {
        String key = "workspaces/w-1/products/p-1/image.webp";
        Instant now = Instant.now();

        HeadObjectResponse headResponse = HeadObjectResponse.builder()
                .contentLength(1024L)
                .contentType("image/webp")
                .lastModified(now)
                .eTag("etag-123")
                .metadata(Map.of("category", "products"))
                .build();

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headResponse);

        StorageMetadata metadata = storageService.getMetadata(key);

        assertNotNull(metadata);
        assertEquals(1024L, metadata.contentLength());
        assertEquals("image/webp", metadata.contentType());
        assertEquals(now, metadata.lastModified());
        assertEquals("etag-123", metadata.eTag());
        assertEquals("products", metadata.userMetadata().get("category"));
    }

    @Test
    void getMetadata_throwsStorageFileNotFoundException_whenMissing() {
        String key = "missing.jpg";
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("Not found").build());

        assertThrows(StorageFileNotFoundException.class, () -> storageService.getMetadata(key));
    }

    @Test
    void getUrl_formatsUrlWithoutDuplicateSlashes() {
        String url1 = storageService.getUrl("workspaces/w-1/products/p-1/image.webp");
        assertEquals("https://files.outvoice.omkarsathe.com/workspaces/w-1/products/p-1/image.webp", url1);

        String url2 = storageService.getUrl("/workspaces/w-1/products/p-1/image.webp");
        assertEquals("https://files.outvoice.omkarsathe.com/workspaces/w-1/products/p-1/image.webp", url2);
    }

    @Test
    void upload_throwsStorageException_whenS3ClientThrows() {
        String key = "workspaces/w-1/products/p-1/image.webp";
        UploadRequest request = new UploadRequest(
                key,
                new ByteArrayInputStream(new byte[]{1}),
                1,
                "image/jpeg",
                Map.of(),
                true
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("Access Denied").statusCode(403).build());

        assertThrows(StorageException.class, () -> storageService.upload(request));
    }
}
