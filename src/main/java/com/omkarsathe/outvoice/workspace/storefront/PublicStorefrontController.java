package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.workspace.storefront.dto.PublicProductDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.PublicStorefrontDto;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontCheckoutRequest;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontCheckoutResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/storefronts")
@RequiredArgsConstructor
public class PublicStorefrontController {

    private final PublicStorefrontService publicStorefrontService;

    @GetMapping("/resolve")
    public ResponseEntity<PublicStorefrontDto> resolveStorefront(
            @RequestParam(required = false) String host,
            @RequestParam(required = false) String slug,
            HttpServletRequest request
    ) {
        String effectiveHost = host;
        if ((effectiveHost == null || effectiveHost.isBlank())) {
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            if (forwardedHost != null && !forwardedHost.isBlank()) {
                effectiveHost = forwardedHost.split(",")[0].trim();
            } else {
                effectiveHost = request.getHeader("Host");
            }
        }

        return ResponseEntity.ok(publicStorefrontService.resolve(effectiveHost, slug));
    }

    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<PublicStorefrontDto> getStorefrontBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(publicStorefrontService.resolveBySlug(slug));
    }

    @GetMapping("/by-host/{hostname}")
    public ResponseEntity<PublicStorefrontDto> getStorefrontByHost(@PathVariable String hostname) {
        return ResponseEntity.ok(publicStorefrontService.resolveByHost(hostname));
    }

    @GetMapping("/{storefrontId}/products")
    public ResponseEntity<List<PublicProductDto>> getProducts(@PathVariable UUID storefrontId) {
        return ResponseEntity.ok(publicStorefrontService.getPublishedProducts(storefrontId));
    }

    @GetMapping("/{storefrontId}/products/{productId}")
    public ResponseEntity<PublicProductDto> getProduct(
            @PathVariable UUID storefrontId,
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(publicStorefrontService.getPublishedProduct(storefrontId, productId));
    }

    @PostMapping("/{storefrontId}/checkout")
    public ResponseEntity<StorefrontCheckoutResponse> checkout(
            @PathVariable UUID storefrontId,
            @Valid @RequestBody StorefrontCheckoutRequest request
    ) {
        return ResponseEntity.ok(publicStorefrontService.checkout(storefrontId, request));
    }
}
