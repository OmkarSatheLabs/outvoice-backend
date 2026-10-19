package com.omkarsathe.outvoice.storage.r2;

import com.omkarsathe.outvoice.storage.ObjectStorageService;
import com.omkarsathe.outvoice.storage.config.StorageProperties;
import com.omkarsathe.outvoice.storage.exception.StorageException;
import com.omkarsathe.outvoice.storage.exception.StorageFileNotFoundException;
import com.omkarsathe.outvoice.storage.model.StorageMetadata;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import com.omkarsathe.outvoice.storage.model.UploadRequest;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Cloudflare R2 implementation of ObjectStorageService using the AWS SDK for Java v2.
 * Connects via Cloudflare's S3-compatible API.
 */
@Slf4j
public class CloudflareR2ObjectStorageService implements ObjectStorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public CloudflareR2ObjectStorageService(S3Client s3Client, StorageProperties properties) {
        this.s3Client = s3Client;
        this.bucket = properties.getR2().getBucket();
        this.publicBaseUrl = cleanBaseUrl(properties.getR2().getPublicBaseUrl());
    }

    @Override
    public void upload(UploadRequest request) {
        try {
            PutObjectRequest.Builder builder = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(request.objectKey())
                    .contentType(request.contentType())
                    .contentLength(request.contentLength());

            if (request.userMetadata() != null && !request.userMetadata().isEmpty()) {
                builder.metadata(request.userMetadata());
            }

            s3Client.putObject(
                    builder.build(),
                    RequestBody.fromInputStream(request.inputStream(), request.contentLength())
            );
            log.debug("Successfully uploaded object to Cloudflare R2: key={}, bucket={}", request.objectKey(), bucket);
        } catch (S3Exception e) {
            log.error("Failed to upload object to Cloudflare R2: key={}, statusCode={}", request.objectKey(), e.statusCode());
            throw new StorageException("Failed to upload object to Cloudflare R2: " + request.objectKey(), e);
        } catch (Exception e) {
            log.error("Unexpected error during R2 upload for key: {}", request.objectKey());
            throw new StorageException("Failed to upload object to Cloudflare R2: " + request.objectKey(), e);
        }
    }

    @Override
    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.debug("Deleted object from Cloudflare R2: key={}, bucket={}", objectKey, bucket);
        } catch (S3Exception e) {
            log.error("Failed to delete object from Cloudflare R2: key={}, statusCode={}", objectKey, e.statusCode());
            throw new StorageException("Failed to delete object from Cloudflare R2: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return false;
        }

        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            log.error("Error checking existence of object in Cloudflare R2: key={}, statusCode={}", objectKey, e.statusCode());
            throw new StorageException("Failed to check object existence in Cloudflare R2: " + objectKey, e);
        }
    }

    @Override
    public StorageMetadata getMetadata(String objectKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);
            Map<String, String> userMetadata = response.metadata() != null ? new HashMap<>(response.metadata()) : Map.of();

            return new StorageMetadata(
                    objectKey,
                    response.contentLength() != null ? response.contentLength() : 0L,
                    response.contentType() != null ? response.contentType() : "application/octet-stream",
                    response.lastModified(),
                    response.eTag(),
                    userMetadata
            );
        } catch (NoSuchKeyException e) {
            throw new StorageFileNotFoundException("R2 object not found: " + objectKey);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new StorageFileNotFoundException("R2 object not found: " + objectKey);
            }
            log.error("Failed to retrieve metadata from Cloudflare R2: key={}, statusCode={}", objectKey, e.statusCode());
            throw new StorageException("Failed to retrieve metadata from Cloudflare R2: " + objectKey, e);
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
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getRequest);
            GetObjectResponse response = s3Stream.response();

            StorageMetadata metadata = new StorageMetadata(
                    objectKey,
                    response.contentLength() != null ? response.contentLength() : 0L,
                    response.contentType() != null ? response.contentType() : "application/octet-stream",
                    response.lastModified(),
                    response.eTag(),
                    response.metadata() != null ? new HashMap<>(response.metadata()) : Map.of()
            );

            return new StorageObject(objectKey, s3Stream, metadata);
        } catch (NoSuchKeyException e) {
            throw new StorageFileNotFoundException("R2 object not found: " + objectKey);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new StorageFileNotFoundException("R2 object not found: " + objectKey);
            }
            log.error("Failed to load object from Cloudflare R2: key={}, statusCode={}", objectKey, e.statusCode());
            throw new StorageException("Failed to load object from Cloudflare R2: " + objectKey, e);
        }
    }

    private String cleanBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Cloudflare R2 public-base-url must be configured");
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
