package com.omkarsathe.outvoice.storage.config;

import com.omkarsathe.outvoice.storage.ObjectStorageService;
import com.omkarsathe.outvoice.storage.local.LocalObjectStorageService;
import com.omkarsathe.outvoice.storage.r2.CloudflareR2ObjectStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@Slf4j
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "outvoice.storage.provider", havingValue = "local", matchIfMissing = true)
    public ObjectStorageService localStorageService(StorageProperties properties) {
        log.info("Initialized Local Object Storage provider with base directory: {}",
                properties.getLocal().getBaseDirectory());
        return new LocalObjectStorageService(properties);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "outvoice.storage.provider", havingValue = "r2")
    public S3Client r2S3Client(StorageProperties properties) {
        validateR2Properties(properties.getR2());

        StorageProperties.R2Properties r2 = properties.getR2();
        String rawEndpoint = r2.getEndpoint();
        URI endpointUri;
        if (rawEndpoint != null && !rawEndpoint.isBlank()) {
            String trimmed = rawEndpoint.trim();
            if (r2.getBucket() != null && !r2.getBucket().isBlank() && trimmed.endsWith("/" + r2.getBucket().trim())) {
                trimmed = trimmed.substring(0, trimmed.length() - ("/" + r2.getBucket().trim()).length());
            }
            endpointUri = URI.create(trimmed);
        } else {
            endpointUri = URI.create("https://" + r2.getAccountId().trim() + ".r2.cloudflarestorage.com");
        }

        return S3Client.builder()
                .endpointOverride(endpointUri)
                .region(Region.of(r2.getRegion() != null && !r2.getRegion().isBlank() ? r2.getRegion().trim() : "auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(r2.getAccessKeyId().trim(), r2.getSecretAccessKey().trim())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .httpClientBuilder(ApacheHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(r2.getConnectionTimeoutSeconds()))
                        .socketTimeout(Duration.ofSeconds(r2.getRequestTimeoutSeconds())))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofSeconds(r2.getRequestTimeoutSeconds()))
                        .retryStrategy(RetryMode.STANDARD)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "outvoice.storage.provider", havingValue = "r2")
    public ObjectStorageService cloudflareR2StorageService(S3Client s3Client, StorageProperties properties) {
        log.info("Initialized Cloudflare R2 Object Storage provider with bucket: {} and publicBaseUrl: {}",
                properties.getR2().getBucket(), properties.getR2().getPublicBaseUrl());
        return new CloudflareR2ObjectStorageService(s3Client, properties);
    }

    public static void validateR2Properties(StorageProperties.R2Properties r2) {
        if (r2 == null) {
            throw new IllegalStateException("Cloudflare R2 storage is selected (outvoice.storage.provider=r2), but no 'outvoice.storage.r2' configuration was provided.");
        }

        List<String> missing = new ArrayList<>();
        if (r2.getBucket() == null || r2.getBucket().isBlank()) {
            missing.add("outvoice.storage.r2.bucket (or CLOUDFLARE_R2_BUCKET)");
        }
        if ((r2.getAccountId() == null || r2.getAccountId().isBlank()) && (r2.getEndpoint() == null || r2.getEndpoint().isBlank())) {
            missing.add("outvoice.storage.r2.account-id (or outvoice.storage.r2.endpoint)");
        }
        if (r2.getAccessKeyId() == null || r2.getAccessKeyId().isBlank()) {
            missing.add("outvoice.storage.r2.access-key-id (or CLOUDFLARE_R2_ACCESS_KEY_ID)");
        }
        if (r2.getSecretAccessKey() == null || r2.getSecretAccessKey().isBlank()) {
            missing.add("outvoice.storage.r2.secret-access-key (or CLOUDFLARE_R2_SECRET_ACCESS_KEY)");
        }
        if (r2.getPublicBaseUrl() == null || r2.getPublicBaseUrl().isBlank()) {
            missing.add("outvoice.storage.r2.public-base-url (or CLOUDFLARE_R2_PUBLIC_BASE_URL)");
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Cloudflare R2 storage is selected (outvoice.storage.provider=r2), but the following required properties are missing: " + missing);
        }
    }
}
