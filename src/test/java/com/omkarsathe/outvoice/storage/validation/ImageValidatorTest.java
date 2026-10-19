package com.omkarsathe.outvoice.storage.validation;

import com.omkarsathe.outvoice.common.exception.BadRequestException;
import com.omkarsathe.outvoice.storage.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ImageValidatorTest {

    private ImageValidator imageValidator;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.getImages().setMaxFileSize(5 * 1024 * 1024);
        properties.getImages().setMaxDimension(8192);
        imageValidator = new ImageValidator(properties);
    }

    @Test
    void validate_acceptsValidJpeg() throws IOException {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        byte[] jpegBytes = baos.toByteArray();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.jpg",
                "image/jpeg",
                jpegBytes
        );

        ValidatedImage validated = imageValidator.validate(file);
        assertNotNull(validated);
        assertEquals("image/jpeg", validated.contentType());
        assertEquals(".jpg", validated.extension());
        assertEquals(100, validated.width());
        assertEquals(100, validated.height());
    }

    @Test
    void validate_acceptsValidPng() throws IOException {
        BufferedImage img = new BufferedImage(50, 80, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] pngBytes = baos.toByteArray();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.png",
                "image/png",
                pngBytes
        );

        ValidatedImage validated = imageValidator.validate(file);
        assertNotNull(validated);
        assertEquals("image/png", validated.contentType());
        assertEquals(".png", validated.extension());
        assertEquals(50, validated.width());
        assertEquals(80, validated.height());
    }

    @Test
    void validate_acceptsValidWebp() {
        // Construct minimal valid WebP VP8 binary
        byte[] webp = new byte[30];
        // RIFF
        webp[0] = 'R'; webp[1] = 'I'; webp[2] = 'F'; webp[3] = 'F';
        // Size
        webp[4] = 22; webp[5] = 0; webp[6] = 0; webp[7] = 0;
        // WEBP
        webp[8] = 'W'; webp[9] = 'E'; webp[10] = 'B'; webp[11] = 'P';
        // VP8_
        webp[12] = 'V'; webp[13] = 'P'; webp[14] = '8'; webp[15] = ' ';
        // Chunk size
        webp[16] = 10; webp[17] = 0; webp[18] = 0; webp[19] = 0;
        // Frame tag
        webp[20] = 0; webp[21] = 0; webp[22] = 0;
        // Start code 9d 01 2a
        webp[23] = (byte) 0x9D; webp[24] = 0x01; webp[25] = 0x2A;
        // Width = 64 (0x40 0x00)
        webp[26] = 64; webp[27] = 0;
        // Height = 32 (0x20 0x00)
        webp[28] = 32; webp[29] = 0;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.webp",
                "image/webp",
                webp
        );

        ValidatedImage validated = imageValidator.validate(file);
        assertNotNull(validated);
        assertEquals("image/webp", validated.contentType());
        assertEquals(".webp", validated.extension());
        assertEquals(64, validated.width());
        assertEquals(32, validated.height());
    }

    @Test
    void validate_rejectsTextDisguisedAsImage() {
        byte[] fakeContent = "This is a malicious text file disguised as an image".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "exploit.jpg",
                "image/jpeg",
                fakeContent
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                imageValidator.validate(file));
        assertTrue(ex.getMessage().contains("Invalid or unsupported image content"));
    }

    @Test
    void validate_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        assertThrows(BadRequestException.class, () -> imageValidator.validate(file));
    }

    @Test
    void validate_rejectsOversizedFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "huge.jpg",
                "image/jpeg",
                new byte[6 * 1024 * 1024] // 6MB > 5MB limit
        );

        assertThrows(BadRequestException.class, () -> imageValidator.validate(file));
    }
}
