package com.omkarsathe.outvoice.platform;

import com.omkarsathe.outvoice.platform.dto.CreatePlatformOrgRequest;
import com.omkarsathe.outvoice.platform.dto.PlatformOrgResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Platform-admin organisation management. All endpoints require a valid platform JWT. */
@RestController
@RequestMapping("/api/platform/orgs")
@RequiredArgsConstructor
public class PlatformOrgController {

    private final PlatformOrgService platformOrgService;

    /** Create an org and assign an owner (creates user if not exists). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlatformOrgResponse createOrg(@Valid @RequestBody CreatePlatformOrgRequest request) {
        return platformOrgService.createOrg(request);
    }

    /** List all orgs with status and member count. */
    @GetMapping
    public List<PlatformOrgResponse> listOrgs() {
        return platformOrgService.listOrgs();
    }

    /** Suspend an org (blocks org-level logins). */
    @PatchMapping("/{orgId}/suspend")
    public PlatformOrgResponse suspendOrg(@PathVariable UUID orgId) {
        return platformOrgService.suspendOrg(orgId);
    }

    /** Re-activate a suspended org. */
    @PatchMapping("/{orgId}/activate")
    public PlatformOrgResponse activateOrg(@PathVariable UUID orgId) {
        return platformOrgService.activateOrg(orgId);
    }
}
