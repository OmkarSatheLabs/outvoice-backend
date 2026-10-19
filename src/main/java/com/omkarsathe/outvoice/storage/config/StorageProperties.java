package com.omkarsathe.outvoice.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "outvoice.storage")
@Getter
@Setter
public class StorageProperties {

    /**
     * Active storage provider: "local" or "r2"
     */
    private String provider = "local";

    private LocalProperties local = new LocalProperties();
    private R2Properties r2 = new R2Properties();
    private ImageProperties images = new ImageProperties();

    @Getter
    @Setter
    public static class LocalProperties {
        private String baseDirectory = "./uploads";
        private String publicBaseUrl = "http://localhost:8080/files";
    }

    @Getter
    @Setter
    public static class R2Properties {
        private String accountId;
        private String bucket;
        private String accessKeyId;
        private String secretAccessKey;
        private String endpoint;
        private String region = "auto";
        private String publicBaseUrl;
        private int connectionTimeoutSeconds = 10;
        private int requestTimeoutSeconds = 30;
    }

    @Getter
    @Setter
    public static class ImageProperties {
        private long maxFileSize = 5 * 1024 * 1024; // 5MB
        private Set<String> allowedMimeTypes = Set.of("image/jpeg", "image/png", "image/webp");
        private int maxDimension = 8192;
    }
}
