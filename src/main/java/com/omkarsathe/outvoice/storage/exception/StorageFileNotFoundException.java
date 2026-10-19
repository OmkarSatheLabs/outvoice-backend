package com.omkarsathe.outvoice.storage.exception;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;

public class StorageFileNotFoundException extends ResourceNotFoundException {

    public StorageFileNotFoundException(String message) {
        super(message);
    }
}
