package com.omkarsathe.outvoice.storage.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageConfigTest {

    @Test
    void validateR2Properties_throwsException_whenRequiredPropertiesMissing() {
        StorageProperties.R2Properties r2 = new StorageProperties.R2Properties();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                StorageConfig.validateR2Properties(r2));

        assertTrue(ex.getMessage().contains("outvoice.storage.r2.bucket"));
        assertTrue(ex.getMessage().contains("outvoice.storage.r2.access-key-id"));
        assertTrue(ex.getMessage().contains("outvoice.storage.r2.secret-access-key"));
        assertTrue(ex.getMessage().contains("outvoice.storage.r2.public-base-url"));
    }

    @Test
    void validateR2Properties_succeeds_whenAllPropertiesProvided() {
        StorageProperties.R2Properties r2 = new StorageProperties.R2Properties();
        r2.setBucket("my-bucket");
        r2.setAccountId("my-account");
        r2.setAccessKeyId("key-123");
        r2.setSecretAccessKey("secret-123");
        r2.setPublicBaseUrl("https://files.outvoice.com");

        assertDoesNotThrow(() -> StorageConfig.validateR2Properties(r2));
    }

    @Test
    void validateR2Properties_succeeds_withCustomEndpointInsteadOfAccountId() {
        StorageProperties.R2Properties r2 = new StorageProperties.R2Properties();
        r2.setBucket("my-bucket");
        r2.setEndpoint("https://custom.endpoint.com");
        r2.setAccessKeyId("key-123");
        r2.setSecretAccessKey("secret-123");
        r2.setPublicBaseUrl("https://files.outvoice.com");

        assertDoesNotThrow(() -> StorageConfig.validateR2Properties(r2));
    }
}
