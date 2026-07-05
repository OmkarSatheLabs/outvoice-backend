package com.omkarsathe.outvoice.workspace.invitation;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.workspace.dto.AcceptInvitationAndSignupRequest;
import com.omkarsathe.outvoice.workspace.dto.InviteUserRequest;
import com.omkarsathe.outvoice.workspace.dto.WorkspaceInvitationResponse;
import com.omkarsathe.outvoice.workspace.dto.WorkspaceInviteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class UserWorkspaceInvitationController {

    private final UserWorkspaceInvitationService userWorkspaceInvitationService;

    @GetMapping("/invites")
    public List<WorkspaceInviteResponse> getInvites(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return userWorkspaceInvitationService.getUserInvites(userId);
    }

    @PostMapping("{workspaceId}/invites/{invoiceId}/accept")
    public void acceptInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID invoiceId,
            @RequestParam String token) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        userWorkspaceInvitationService.acceptInvitation(userId, token);
    }

    @PostMapping("{workspaceId}/invites/{inviteId}/decline")
    public void declineInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID inviteId,
            @RequestParam String token) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        userWorkspaceInvitationService.declineInvitation(userId, workspaceId, inviteId, token);
    }

    @PostMapping("/{workspaceId}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceInvitationResponse inviteUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteUserRequest request) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return userWorkspaceInvitationService.inviteUser(currentUserId, workspaceId, request);
    }

    @GetMapping("/{workspaceId}/invites")
    public java.util.List<WorkspaceInvitationResponse> getInvitations(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return userWorkspaceInvitationService.getInvitations(currentUserId, workspaceId);
    }

    @GetMapping("/invites/resolve")
    public WorkspaceInvitationResponse resolveInvitation(@RequestParam String token) {
        return userWorkspaceInvitationService.getInvitationByToken(token);
    }

    @PostMapping("/invites/accept-by-token")
    public void acceptInvitation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String token) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        userWorkspaceInvitationService.acceptInvitation(currentUserId, token);
    }

    @PostMapping("/invites/signup-and-accept")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signupAndAcceptInvitation(@Valid @RequestBody AcceptInvitationAndSignupRequest request) {
        return userWorkspaceInvitationService.signupAndAcceptInvitation(request);
    }
}
