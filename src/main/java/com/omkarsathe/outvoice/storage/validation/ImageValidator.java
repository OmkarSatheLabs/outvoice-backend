package com.omkarsathe.outvoice.storage.validation;

import com.omkarsathe.outvoice.common.exception.BadRequestException;
import com.omkarsathe.outvoice.storage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageValidator {

    private final StorageProperties storageProperties;

    // Magic bytes
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_MAGIC = new byte[]{'R', 'I', 'F', 'F'};
    private static final byte[] WEBP_MAGIC = new byte[]{'W', 'E', 'B', 'P'};

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file cannot be empty");
        }

        long maxFileSize = storageProperties.getImages().getMaxFileSize();
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("Image file size (" + (file.getSize() / 1024 / 1024) + "MB) exceeds maximum limit of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Failed to read image file data");
        }

        if (bytes.length < 12) {
            throw new BadRequestException("Image file is too small or malformed");
        }

        // Sniff format using magic numbers
        DetectedFormat format = detectFormat(bytes);
        if (format == null) {
            throw new BadRequestException("Invalid or unsupported image content. Allowed formats: JPEG, PNG, WebP.");
        }

        if (!storageProperties.getImages().getAllowedMimeTypes().contains(format.mimeType)) {
            throw new BadRequestException("Image type " + format.mimeType + " is not in the allowed list: " + storageProperties.getImages().getAllowedMimeTypes());
        }

        // Validate dimensions
        int[] dimensions = extractDimensions(bytes, format);
        int width = dimensions[0];
        int height = dimensions[1];
        int maxDimension = storageProperties.getImages().getMaxDimension();

        if (width <= 0 || height <= 0) {
            throw new BadRequestException("Could not read valid image dimensions. File may be corrupted.");
        }

        if (width > maxDimension || height > maxDimension) {
            throw new BadRequestException("Image dimensions (" + width + "x" + height + ") exceed the maximum allowed limit of " + maxDimension + "px.");
        }

        return new ValidatedImage(
                format.mimeType,
                format.extension,
                width,
                height,
                bytes.length,
                new ByteArrayInputStream(bytes)
        );
    }

    private DetectedFormat detectFormat(byte[] bytes) {
        if (bytes.length >= 3 && bytes[0] == JPEG_MAGIC[0] && bytes[1] == JPEG_MAGIC[1] && bytes[2] == JPEG_MAGIC[2]) {
            return new DetectedFormat("image/jpeg", ".jpg");
        }

        if (bytes.length >= 8 && startsWith(bytes, PNG_MAGIC)) {
            return new DetectedFormat("image/png", ".png");
        }

        if (bytes.length >= 12 && startsWith(bytes, RIFF_MAGIC) && hasSubArrayAt(bytes, WEBP_MAGIC, 8)) {
            return new DetectedFormat("image/webp", ".webp");
        }

        return null;
    }

    private int[] extractDimensions(byte[] bytes, DetectedFormat format) {
        // Attempt using ImageIO first
        try {
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(bytes));
            if (bi != null) {
                return new int[]{bi.getWidth(), bi.getHeight()};
            }
        } catch (Exception ignored) {
        }

        // Fallback for WebP or formats not supported by installed ImageIO readers
        if ("image/webp".equals(format.mimeType)) {
            int[] webpDim = parseWebpDimensions(bytes);
            if (webpDim != null) {
                return webpDim;
            }
        }

        return new int[]{-1, -1};
    }

    private int[] parseWebpDimensions(byte[] bytes) {
        try {
            if (bytes.length < 30) return null;
            String chunkType = new String(bytes, 12, 4);

            if ("VP8 ".equals(chunkType)) {
                // Lossy VP8: frame tag is at offset 20 (3 bytes), start code 0x9d 0x01 0x2a at 23..25
                int width = ((bytes[26] & 0xFF) | ((bytes[27] & 0xFF) << 8)) & 0x3FFF;
                int height = ((bytes[28] & 0xFF) | ((bytes[29] & 0xFF) << 8)) & 0x3FFF;
                return new int[]{width, height};
            } else if ("VP8L".equals(chunkType)) {
                // Lossless VP8L: signature 0x2f at offset 20
                if ((bytes[20] & 0xFF) == 0x2F) {
                    int b0 = bytes[21] & 0xFF;
                    int b1 = bytes[22] & 0xFF;
                    int b2 = bytes[23] & 0xFF;
                    int b3 = bytes[24] & 0xFF;
                    int width = 1 + (((b1 & 0x3F) << 8) | b0);
                    int height = 1 + (((b3 & 0x0F) << 10) | (b2 << 2) | ((b1 & 0xC0) >> 6));
                    return new int[]{width, height};
                }
            } else if ("VP8X".equals(chunkType)) {
                // Extended VP8X: canvas width at 24..26 (24-bit little endian) + 1, height at 27..29 + 1
                int width = 1 + (bytes[24] & 0xFF | ((bytes[25] & 0xFF) << 8) | ((bytes[26] & 0xFF) << 16));
                int height = 1 + (bytes[27] & 0xFF | ((bytes[28] & 0xFF) << 8) | ((bytes[29] & 0xFF) << 16));
                return new int[]{width, height};
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean startsWith(byte[] source, byte[] match) {
        if (source.length < match.length) return false;
        for (int i = 0; i < match.length; i++) {
            if (source[i] != match[i]) return false;
        }
        return true;
    }

    private static boolean hasSubArrayAt(byte[] source, byte[] match, int offset) {
        if (source.length < offset + match.length) return false;
        for (int i = 0; i < match.length; i++) {
            if (source[offset + i] != match[i]) return false;
        }
        return true;
    }

    private record DetectedFormat(String mimeType, String extension) {}
}
