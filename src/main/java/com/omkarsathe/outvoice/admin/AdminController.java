package com.omkarsathe.outvoice.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("workspaces")
    public List<AdminWorkspaceResponse> getWorkspaces() {
        return adminService.getWorkspaces();
    }

    @PostMapping("workspaces")
    public AdminWorkspaceResponse createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        return adminService.createWorkspace(request);
    }

    @GetMapping("workspaces/{workspaceId}/members")
    public List<AdminWorkspaceMemberResponse> getWorkspaceMembers(@PathVariable("workspaceId") UUID workspaceId) {
        return adminService.getWorkspaceMembers(workspaceId);
    }

    @PutMapping("workspaces/{workspaceId}")
    public AdminWorkspaceResponse updateWorkspace(
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody AdminUpdateWorkspaceRequest request
    ) {
        return adminService.updateWorkspace(workspaceId, request);
    }

    @PutMapping("workspaces/{workspaceId}/status")
    public ResponseEntity<Map<String, Object>> toggleSuspend(
            @PathVariable("workspaceId") UUID workspaceId
    ) {
        boolean suspended = adminService.toggleSuspend(workspaceId);
        return ResponseEntity.ok(Map.of("suspended", suspended));
    }

    @DeleteMapping("workspaces/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable("workspaceId") UUID workspaceId
    ) {
        adminService.deleteWorkspace(workspaceId);
        return ResponseEntity.noContent().build();
    }
}
