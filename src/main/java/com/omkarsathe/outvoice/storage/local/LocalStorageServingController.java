package com.omkarsathe.outvoice.storage.local;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.storage.ObjectStorageService;
import com.omkarsathe.outvoice.storage.exception.StorageFileNotFoundException;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.time.Duration;

/**
 * Controller for serving locally stored public assets in development mode.
 * Only enabled when the local storage provider is active.
 */
@RestController
@RequestMapping("/files")
@ConditionalOnProperty(name = "outvoice.storage.provider", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class LocalStorageServingController {

    private final ObjectStorageService storageService;

    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (path == null) {
            throw new ResourceNotFoundException("File path not provided");
        }

        // Strip the "/files" prefix to get the canonical object key
        String objectKey = path.replaceFirst("^/files/?", "");
        if (objectKey.isBlank() || objectKey.contains("..")) {
            throw new ResourceNotFoundException("Invalid file key: " + objectKey);
        }

        try {
            StorageObject storageObject = storageService.load(objectKey);
            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(storageObject.metadata().contentType());
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(storageObject.metadata().contentLength())
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                    .body(new InputStreamResource(storageObject.inputStream()));
        } catch (StorageFileNotFoundException e) {
            throw new ResourceNotFoundException("File not found: " + objectKey);
        }
    }
}
