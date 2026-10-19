package com.omkarsathe.outvoice.workspace.product;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductImageStorageService {

    private final Path uploadLocation;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public ProductImageStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("products");
        try {
            Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize product image upload folder", e);
        }
    }

    public String storeImage(UUID productId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file size exceeds the 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid image type. Allowed types: JPEG, PNG, WEBP, GIF");
        }

        String extension = getExtension(contentType, file.getOriginalFilename());
        deleteExistingFiles(productId);

        String filename = productId.toString() + extension;
        Path targetPath = this.uploadLocation.resolve(filename);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store product image", e);
        }

        return "/api/public/products/" + productId + "/image?v=" + System.currentTimeMillis();
    }

    public Resource loadImageAsResource(UUID productId) {
        Path filePath = findExistingFile(productId);
        if (filePath == null || !Files.exists(filePath)) {
            throw new ResourceNotFoundException("Product image not found for product: " + productId);
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Could not read product image for product: " + productId);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Product image not found for product: " + productId);
        }
    }

    public void deleteImage(UUID productId) {
        deleteExistingFiles(productId);
    }

    private Path findExistingFile(UUID productId) {
        String prefix = productId.toString() + ".";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.uploadLocation, prefix + "*")) {
            for (Path entry : stream) {
                return entry;
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private void deleteExistingFiles(UUID productId) {
        String prefix = productId.toString() + ".";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.uploadLocation, prefix + "*")) {
            for (Path entry : stream) {
                Files.deleteIfExists(entry);
            }
        } catch (IOException ignored) {
        }
    }

    private String getExtension(String contentType, String originalFilename) {
        if (contentType != null) {
            switch (contentType.toLowerCase()) {
                case "image/jpeg": return ".jpg";
                case "image/png": return ".png";
                case "image/webp": return ".webp";
                case "image/gif": return ".gif";
            }
        }
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return ".jpg";
    }
}
