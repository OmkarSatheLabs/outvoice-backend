package com.omkarsathe.outvoice.storage.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public record StorageMetadata(
        String objectKey,
        long contentLength,
        String contentType,
        Instant lastModified,
        String eTag,
        Map<String, String> userMetadata
) {
    public StorageMetadata {
        if (userMetadata == null) {
            userMetadata = Collections.emptyMap();
        }
    }
}
