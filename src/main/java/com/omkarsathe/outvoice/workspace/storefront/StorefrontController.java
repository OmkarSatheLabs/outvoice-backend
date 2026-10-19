package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.workspace.storefront.dto.AddStorefrontDomainRequest;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontDomainResponse;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontResponse;
import com.omkarsathe.outvoice.workspace.storefront.dto.UpdateStorefrontRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/storefront")
@RequiredArgsConstructor
public class StorefrontController {

    private final StorefrontService storefrontService;

    @GetMapping
    public ResponseEntity<StorefrontResponse> getStorefront(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(storefrontService.getOrCreateStorefront(workspaceId, userId));
    }

    @PutMapping
    public ResponseEntity<StorefrontResponse> updateStorefront(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateStorefrontRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(storefrontService.updateStorefront(workspaceId, request, userId));
    }

    @PostMapping("/publish")
    public ResponseEntity<StorefrontResponse> publishStorefront(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(storefrontService.publishStorefront(workspaceId, userId));
    }

    @PostMapping("/unpublish")
    public ResponseEntity<StorefrontResponse> unpublishStorefront(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(storefrontService.unpublishStorefront(workspaceId, userId));
    }

    @PostMapping("/toggle")
    public ResponseEntity<StorefrontResponse> toggleStorefront(
            @PathVariable UUID workspaceId,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = extractUserId(userDetails);
        boolean enabled = body.getOrDefault("enabled", true);
        return ResponseEntity.ok(storefrontService.toggleStorefront(workspaceId, enabled, userId));
    }

    @GetMapping("/domains")
    public ResponseEntity<List<StorefrontDomainResponse>> getDomains(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(storefrontService.getDomains(workspaceId, userId));
    }

    @PostMapping("/domains")
    public ResponseEntity<StorefrontDomainResponse> addDomain(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody AddStorefrontDomainRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(storefrontService.addCustomDomain(workspaceId, request, userId));
    }

    @PostMapping("/domains/{domainId}/verify")
    public ResponseEntity<StorefrontDomainResponse> verifyDomain(
            @PathVariable UUID workspaceId,
            @PathVariable UUID domainId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(storefrontService.verifyCustomDomain(workspaceId, domainId, userId));
    }

    @DeleteMapping("/domains/{domainId}")
    public ResponseEntity<Void> deleteDomain(
            @PathVariable UUID workspaceId,
            @PathVariable UUID domainId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = extractUserId(userDetails);
        storefrontService.deleteCustomDomain(workspaceId, domainId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(UserDetails userDetails) {
        return userDetails != null ? UUID.fromString(userDetails.getUsername()) : null;
    }
}
