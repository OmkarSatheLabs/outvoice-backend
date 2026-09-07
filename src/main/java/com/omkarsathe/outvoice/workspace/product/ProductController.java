package com.omkarsathe.outvoice.workspace.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
