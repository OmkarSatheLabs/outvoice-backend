package com.omkarsathe.outvoice.storage.validation;

import java.io.InputStream;

public record ValidatedImage(
        String contentType,
        String extension,
        int width,
        int height,
        long contentLength,
        InputStream inputStream
) {}
