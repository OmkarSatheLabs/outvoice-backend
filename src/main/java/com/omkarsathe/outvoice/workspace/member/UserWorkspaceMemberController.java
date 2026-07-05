package com.omkarsathe.outvoice.workspace.member;

import com.omkarsathe.outvoice.workspace.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
@RequiredArgsConstructor
public class UserWorkspaceMemberController {

    private final UserWorkspaceMemberService workspaceMemberService;

    // --- TEAMS CRUD ---

    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody TeamRequest request) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return workspaceMemberService.createTeam(currentUserId, workspaceId, request);
    }

    @GetMapping("/teams")
    public List<TeamResponse> getTeams(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return workspaceMemberService.getTeams(currentUserId, workspaceId);
    }

    @PutMapping("/teams/{teamId}")
    public TeamResponse updateTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID teamId,
            @Valid @RequestBody TeamRequest request) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return workspaceMemberService.updateTeam(currentUserId, workspaceId, teamId, request);
    }

    @DeleteMapping("/teams/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID teamId) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        workspaceMemberService.deleteTeam(currentUserId, workspaceId, teamId);
    }

    // --- CUSTOM ROLES CRUD ---

    @PostMapping("/custom-roles")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomRoleResponse createCustomRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CustomRoleRequest request) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return workspaceMemberService.createCustomRole(currentUserId, workspaceId, request);
    }

    @GetMapping("/custom-roles")
    public List<CustomRoleResponse> getCustomRoles(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return workspaceMemberService.getCustomRoles(currentUserId, workspaceId);
    }

    @PutMapping("/custom-roles/{roleId}")
    public CustomRoleResponse updateCustomRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID roleId,
            @Valid @RequestBody CustomRoleRequest request) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return workspaceMemberService.updateCustomRole(currentUserId, workspaceId, roleId, request);
    }

    @DeleteMapping("/custom-roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID roleId) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        workspaceMemberService.deleteCustomRole(currentUserId, workspaceId, roleId);
    }

    // --- MEMBERS MANAGEMENT ---

    @GetMapping("/members")
    public List<WorkspaceMemberResponse> getMembers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        return workspaceMemberService.getMembers(currentUserId, workspaceId);
    }

    @PutMapping("/members/{memberUserId}/roles-teams")
    public void updateMemberTeamsAndRoles(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberUserId,
            @Valid @RequestBody UpdateMemberRolesTeamsRequest request) {
        UUID currentUserId = UUID.fromString(userDetails.getUsername());
        workspaceMemberService.updateMemberTeamsAndRoles(currentUserId, workspaceId, memberUserId, request);
    }
}
