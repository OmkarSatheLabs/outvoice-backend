package com.omkarsathe.outvoice.storage.local;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.storage.ObjectStorageService;
import com.omkarsathe.outvoice.storage.exception.StorageFileNotFoundException;
import com.omkarsathe.outvoice.storage.model.StorageMetadata;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalStorageServingControllerTest {

    @Mock
    private ObjectStorageService storageService;

    private LocalStorageServingController controller;

    @BeforeEach
    void setUp() {
        controller = new LocalStorageServingController(storageService);
    }

    @Test
    void serveFile_returnsResourceWithCorrectHeaders() {
        String key = "workspaces/ws-1/products/p-1/image.jpg";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/files/" + key);

        StorageMetadata metadata = new StorageMetadata(
                key, 100L, "image/jpeg", Instant.now(), null, Map.of()
        );
        StorageObject storageObject = new StorageObject(
                key, new ByteArrayInputStream(new byte[100]), metadata
        );
        when(storageService.load(key)).thenReturn(storageObject);

        ResponseEntity<Resource> response = controller.serveFile(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
        assertEquals(100L, response.getHeaders().getContentLength());
        assertTrue(response.getHeaders().getCacheControl().contains("max-age=604800"));
    }

    @Test
    void serveFile_throwsNotFound_whenObjectMissing() {
        String key = "workspaces/ws-1/products/p-1/missing.png";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/files/" + key);

        when(storageService.load(key)).thenThrow(new StorageFileNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class, () -> controller.serveFile(request));
    }

    @Test
    void serveFile_throwsNotFound_whenPathTraversalAttempted() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "/files/../secret.txt");

        assertThrows(ResourceNotFoundException.class, () -> controller.serveFile(request));
        verifyNoInteractions(storageService);
    }
}
