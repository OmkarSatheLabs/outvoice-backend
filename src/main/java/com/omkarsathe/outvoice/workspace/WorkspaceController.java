package com.omkarsathe.outvoice.workspace;

import com.omkarsathe.outvoice.workspace.member.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/workspaces")
    public List<WorkspaceResponse> getWorkspaces() {
        return workspaceService.getWorkspaces();
    }

    @GetMapping("/workspaces/{workspaceId}/members")
    public List<MemberResponse> getMembers(@PathVariable UUID workspaceId) {
        return workspaceService.getMembers(workspaceId);
    }

    @GetMapping("/user/workspaces")
    public List<WorkspaceResponse> getUserWorkspaces(@AuthenticationPrincipal UserDetails userDetails) {
        return workspaceService.getUserWorkspaces(UUID.fromString(userDetails.getUsername()));
    }

//    @GetMapping("/admin/workspaces")
//    public List<AdminWorkspaceDto> getAllWorkspaces(@AuthenticationPrincipal UserDetails userDetails) {
//        UUID userId = UUID.fromString(userDetails.getUsername());
//        if (!workspaceService.isPlatformAdmin(userId)) {
//            throw new ForbiddenException("ACCESS_DENIED", "Only platform administrators can access this endpoint");
//        }
//        return workspaceService.getAllWorkspaces();
//    }
//
//    @PostMapping("/workspaces")
//    public WorkspaceResponse createWorkspace(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody WorkspaceRequest workspaceRequest) {
//        return workspaceService.createUserWorkspace(UUID.fromString(userDetails.getUsername()), workspaceRequest);
//    }
//
//    @PostMapping("/workspaces/invites/{inviteId}/accept")
//    public void acceptInvite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID inviteId) {
//        UUID userId = UUID.fromString(userDetails.getUsername());
//        workspaceService.acceptInvite(userId, inviteId);
//    }
//
//    @PostMapping("/workspaces/invites/{inviteId}/decline")
//    public void declineInvite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID inviteId) {
//        UUID userId = UUID.fromString(userDetails.getUsername());
//        workspaceService.declineInvite(userId, inviteId);
//    }
}
