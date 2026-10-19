package com.omkarsathe.outvoice.storage.local;

import com.omkarsathe.outvoice.storage.ObjectStorageService;
import com.omkarsathe.outvoice.storage.config.StorageProperties;
import com.omkarsathe.outvoice.storage.exception.StorageException;
import com.omkarsathe.outvoice.storage.exception.StorageFileNotFoundException;
import com.omkarsathe.outvoice.storage.model.StorageMetadata;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import com.omkarsathe.outvoice.storage.model.UploadRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.util.Collections;

/**
 * Local filesystem implementation of ObjectStorageService.
 * Intended for development and local testing.
 * Files stored locally may be cleared if the local directory is reset.
 */
@Slf4j
public class LocalObjectStorageService implements ObjectStorageService {

    private final Path baseDirectory;
    private final String publicBaseUrl;

    public LocalObjectStorageService(StorageProperties properties) {
        String baseDir = properties.getLocal().getBaseDirectory();
        Path targetDir = Paths.get(baseDir != null && !baseDir.isBlank() ? baseDir : "./uploads")
                .toAbsolutePath()
                .normalize();
        this.publicBaseUrl = cleanBaseUrl(properties.getLocal().getPublicBaseUrl());

        Path establishedDir;
        try {
            Files.createDirectories(targetDir);
            establishedDir = targetDir;
        } catch (Exception e) {
            log.warn("Could not create local storage directory {}: {}. Attempting fallback to temp directory...", targetDir, e.getMessage());
            try {
                Path fallback = Paths.get(System.getProperty("java.io.tmpdir", "/tmp"))
                        .resolve("outvoice-uploads")
                        .toAbsolutePath()
                        .normalize();
                Files.createDirectories(fallback);
                establishedDir = fallback;
                log.info("Using fallback local storage directory: {}", establishedDir);
            } catch (Exception ex) {
                throw new StorageException("Could not initialize local storage directory: " + targetDir, e);
            }
        }
        this.baseDirectory = establishedDir;
    }

    @Override
    public void upload(UploadRequest request) {
        Path targetPath = resolveAndValidate(request.objectKey());
        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            // Write using standard copy option with replace
            Files.copy(request.inputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Successfully saved object locally: {}", request.objectKey());
        } catch (IOException e) {
            throw new StorageException("Failed to store local object: " + request.objectKey(), e);
        }
    }

    @Override
    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        Path targetPath = resolveAndValidate(objectKey);
        try {
            boolean deleted = Files.deleteIfExists(targetPath);
            if (deleted) {
                log.debug("Deleted local object: {}", objectKey);
                cleanEmptyParentDirectories(targetPath.getParent());
            }
        } catch (IOException e) {
            throw new StorageException("Failed to delete local object: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return false;
        }
        Path targetPath = resolveAndValidate(objectKey);
        return Files.isRegularFile(targetPath);
    }

    @Override
    public StorageMetadata getMetadata(String objectKey) {
        Path targetPath = resolveAndValidate(objectKey);
        if (!Files.isRegularFile(targetPath)) {
            throw new StorageFileNotFoundException("Local object not found: " + objectKey);
        }

        try {
            long size = Files.size(targetPath);
            Instant lastModified = Files.getLastModifiedTime(targetPath).toInstant();
            String contentType = Files.probeContentType(targetPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return new StorageMetadata(
                    objectKey,
                    size,
                    contentType,
                    lastModified,
                    null,
                    Collections.emptyMap()
            );
        } catch (IOException e) {
            throw new StorageException("Failed to read metadata for local object: " + objectKey, e);
        }
    }

    @Override
    public String getUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        String cleanKey = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        return publicBaseUrl + "/" + cleanKey;
    }

    @Override
    public StorageObject load(String objectKey) {
        Path targetPath = resolveAndValidate(objectKey);
        if (!Files.isRegularFile(targetPath)) {
            throw new StorageFileNotFoundException("Local object not found: " + objectKey);
        }

        try {
            InputStream is = new BufferedInputStream(Files.newInputStream(targetPath));
            StorageMetadata metadata = getMetadata(objectKey);
            return new StorageObject(objectKey, is, metadata);
        } catch (IOException e) {
            throw new StorageException("Failed to open input stream for local object: " + objectKey, e);
        }
    }

    private Path resolveAndValidate(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("Object key must not be blank");
        }

        if (objectKey.contains("..") || objectKey.startsWith("/") || objectKey.startsWith("\\")) {
            throw new IllegalArgumentException("Invalid object key (path traversal attempted): " + objectKey);
        }

        Path resolved = this.baseDirectory.resolve(objectKey).normalize();
        if (!resolved.startsWith(this.baseDirectory)) {
            throw new IllegalArgumentException("Path traversal outside storage directory: " + objectKey);
        }

        return resolved;
    }

    private void cleanEmptyParentDirectories(Path directory) {
        try {
            while (directory != null && !directory.equals(this.baseDirectory) && directory.startsWith(this.baseDirectory)) {
                try (var entries = Files.list(directory)) {
                    if (entries.findAny().isEmpty()) {
                        Files.deleteIfExists(directory);
                        directory = directory.getParent();
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String cleanBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8080/files";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
