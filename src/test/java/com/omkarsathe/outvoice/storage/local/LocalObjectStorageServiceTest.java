package com.omkarsathe.outvoice.storage.local;

import com.omkarsathe.outvoice.storage.config.StorageProperties;
import com.omkarsathe.outvoice.storage.exception.StorageFileNotFoundException;
import com.omkarsathe.outvoice.storage.model.StorageMetadata;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import com.omkarsathe.outvoice.storage.model.UploadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocalObjectStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalObjectStorageService storageService;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.getLocal().setBaseDirectory(tempDir.toString());
        properties.getLocal().setPublicBaseUrl("http://localhost:8080/files/");
        storageService = new LocalObjectStorageService(properties);
    }

    @Test
    void uploadAndLoad_storesAndRetrievesObjectSuccessfully() throws IOException {
        String key = "workspaces/ws-1/products/p-1/test-image.jpg";
        byte[] content = "test-image-binary-data".getBytes(StandardCharsets.UTF_8);

        UploadRequest request = new UploadRequest(
                key,
                new ByteArrayInputStream(content),
                content.length,
                "image/jpeg",
                Map.of("workspaceId", "ws-1"),
                true
        );

        storageService.upload(request);

        assertTrue(storageService.exists(key));

        StorageMetadata metadata = storageService.getMetadata(key);
        assertNotNull(metadata);
        assertEquals(content.length, metadata.contentLength());

        try (StorageObject object = storageService.load(key)) {
            assertNotNull(object);
            byte[] loadedBytes = object.inputStream().readAllBytes();
            assertArrayEquals(content, loadedBytes);
        }
    }

    @Test
    void delete_removesObjectSuccessfully() {
        String key = "workspaces/ws-1/products/p-1/delete-me.png";
        byte[] content = "content".getBytes(StandardCharsets.UTF_8);

        storageService.upload(new UploadRequest(
                key,
                new ByteArrayInputStream(content),
                content.length,
                "image/png",
                Map.of(),
                true
        ));

        assertTrue(storageService.exists(key));

        storageService.delete(key);

        assertFalse(storageService.exists(key));
        assertThrows(StorageFileNotFoundException.class, () -> storageService.getMetadata(key));
    }

    @Test
    void pathTraversal_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                storageService.upload(new UploadRequest(
                        "../../etc/passwd",
                        new ByteArrayInputStream(new byte[]{1}),
                        1,
                        "text/plain",
                        Map.of(),
                        false
                )));

        assertThrows(IllegalArgumentException.class, () ->
                storageService.load("../outside.txt"));

        assertThrows(IllegalArgumentException.class, () ->
                storageService.delete("/absolute/path"));
    }

    @Test
    void getUrl_formatsUrlWithoutDuplicateSlashes() {
        String url1 = storageService.getUrl("workspaces/ws-1/products/p-1/img.webp");
        assertEquals("http://localhost:8080/files/workspaces/ws-1/products/p-1/img.webp", url1);

        String url2 = storageService.getUrl("/workspaces/ws-1/products/p-1/img.webp");
        assertEquals("http://localhost:8080/files/workspaces/ws-1/products/p-1/img.webp", url2);
    }
}
