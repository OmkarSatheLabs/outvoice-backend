package com.omkarsathe.outvoice.admin;

import com.omkarsathe.outvoice.workspace.CreateWorkspace;
import com.omkarsathe.outvoice.workspace.WorkspaceResponse;
import com.omkarsathe.outvoice.workspace.member.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;
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
}
