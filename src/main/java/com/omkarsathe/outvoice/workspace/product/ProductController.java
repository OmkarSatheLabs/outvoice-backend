package com.omkarsathe.outvoice.workspace.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ProductResponse create(@PathVariable UUID workspaceId, @Valid @RequestBody CreateProduct request) {
        return productService.create(workspaceId, request);
    }

    @GetMapping
    public List<ProductResponse> getProducts(@PathVariable UUID workspaceId) {
        return productService.getProducts(workspaceId);
    }

    @PutMapping("/{productId}")
    public ProductResponse update(
            @PathVariable UUID workspaceId,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProduct request
    ) {
        return productService.update(workspaceId, productId, request);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID workspaceId,
            @PathVariable UUID productId
    ) {
        productService.delete(workspaceId, productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/image")
    public ProductResponse uploadImage(
            @PathVariable UUID workspaceId,
            @PathVariable UUID productId,
            @RequestParam("file") MultipartFile file
    ) {
        return productService.uploadImage(workspaceId, productId, file);
    }

    @DeleteMapping("/{productId}/image")
    public ProductResponse removeImage(
            @PathVariable UUID workspaceId,
            @PathVariable UUID productId
    ) {
        return productService.removeImage(workspaceId, productId);
    }
}
