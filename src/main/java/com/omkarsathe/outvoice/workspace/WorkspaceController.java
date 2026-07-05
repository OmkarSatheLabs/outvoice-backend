package com.omkarsathe.outvoice.workspace;

import com.omkarsathe.outvoice.workspace.dto.WorkspaceInviteResponse;
import com.omkarsathe.outvoice.workspace.dto.WorkspaceRequest;
import com.omkarsathe.outvoice.workspace.dto.WorkspaceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public List<WorkspaceResponse> getWorkspaces(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return workspaceService.getUserWorkspaces(userId);
    }

    @PostMapping
    public WorkspaceResponse createWorkspace(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody WorkspaceRequest workspaceRequest) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return workspaceService.createUserWorkspace(userId, workspaceRequest);
    }

//    @GetMapping("/invites")
//    public List<WorkspaceInviteResponse> getInvites(@AuthenticationPrincipal UserDetails userDetails) {
//        UUID userId = UUID.fromString(userDetails.getUsername());
//        return workspaceService.getUserInvites(userId);
//    }

    @PostMapping("/invites/{inviteId}/accept")
    public void acceptInvite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID inviteId) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        workspaceService.acceptInvite(userId, inviteId);
    }

    @PostMapping("/invites/{inviteId}/decline")
    public void declineInvite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID inviteId) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        workspaceService.declineInvite(userId, inviteId);
    }
}
