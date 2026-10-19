package com.omkarsathe.outvoice.storage.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public record StorageObject(
        String objectKey,
        InputStream inputStream,
        StorageMetadata metadata
) implements Closeable {

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
