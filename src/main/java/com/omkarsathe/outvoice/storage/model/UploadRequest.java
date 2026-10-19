package com.omkarsathe.outvoice.storage.model;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public record UploadRequest(
        String objectKey,
        InputStream inputStream,
        long contentLength,
        String contentType,
        Map<String, String> userMetadata,
        boolean isPublic
) {
    public UploadRequest {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("Object key must not be blank");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream must not be null");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException("Content length must be non-negative");
        }
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        if (userMetadata == null) {
            userMetadata = Collections.emptyMap();
        }
    }
}
