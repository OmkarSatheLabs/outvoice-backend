package com.omkarsathe.outvoice.storage;

import com.omkarsathe.outvoice.storage.model.StorageMetadata;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import com.omkarsathe.outvoice.storage.model.UploadRequest;

public interface ObjectStorageService {

    /**
     * Upload an object to storage using the provided request parameters.
     *
     * @param request the upload parameters including object key, stream, length, and metadata
     */
    void upload(UploadRequest request);

    /**
     * Delete an object by its object key.
     *
     * @param objectKey canonical object key
     */
    void delete(String objectKey);

    /**
     * Check whether an object exists in storage.
     *
     * @param objectKey canonical object key
     * @return true if the object exists, false otherwise
     */
    boolean exists(String objectKey);

    /**
     * Retrieve metadata for an object.
     *
     * @param objectKey canonical object key
     * @return object metadata
     */
    StorageMetadata getMetadata(String objectKey);

    /**
     * Generate the public or client-accessible URL for an object key according to the active provider.
     *
     * @param objectKey canonical object key
     * @return generated URL
     */
    String getUrl(String objectKey);

    /**
     * Load an object for reading or streaming.
     *
     * @param objectKey canonical object key
     * @return storage object container with stream and metadata
     */
    StorageObject load(String objectKey);
}
