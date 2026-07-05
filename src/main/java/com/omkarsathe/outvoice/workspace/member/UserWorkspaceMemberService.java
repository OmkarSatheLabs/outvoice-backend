package com.omkarsathe.outvoice.workspace.member;

import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
import com.omkarsathe.outvoice.workspace.WorkspaceEntity;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.dto.*;
import com.omkarsathe.outvoice.workspace.role.CustomRole;
import com.omkarsathe.outvoice.workspace.role.CustomRoleRepository;
import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import com.omkarsathe.outvoice.workspace.team.Team;
import com.omkarsathe.outvoice.workspace.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserWorkspaceMemberService {

    private final UserWorkspaceRepository userWorkspaceRepository;
    private final TeamRepository teamRepository;
    private final CustomRoleRepository customRoleRepository;
    private final WorkspaceRepository workspaceRepository;

    private void checkOwnerOrAdmin(UUID userId, UUID workspaceId) {
        UserWorkspaceEntity rel = userWorkspaceRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new RuntimeException("Access denied: Not a member of this workspace"));
        if (rel.getRole() != WorkspaceRole.OWNER && rel.getRole() != WorkspaceRole.ADMIN) {
            throw new RuntimeException("Access denied: Only owners or admins can perform this action");
        }
    }

    private void verifyWorkspaceMember(UUID userId, UUID workspaceId) {
        userWorkspaceRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new RuntimeException("Access denied: Not a member of this workspace"));
    }

    // --- TEAMS CRUD ---

    @Transactional
    public TeamResponse createTeam(UUID currentUserId, UUID workspaceId, TeamRequest request) {
        checkOwnerOrAdmin(currentUserId, workspaceId);
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("WorkspaceEntity not found"));

        Team team = Team.builder()
                .workspace(workspace)
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        Team saved = teamRepository.save(team);
        return mapToTeamResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeams(UUID currentUserId, UUID workspaceId) {
        verifyWorkspaceMember(currentUserId, workspaceId);
        return teamRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToTeamResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamResponse updateTeam(UUID currentUserId, UUID workspaceId, UUID teamId, TeamRequest request) {
        checkOwnerOrAdmin(currentUserId, workspaceId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getWorkspace().getId().equals(workspaceId)) {
            throw new RuntimeException("Team does not belong to this workspace");
        }

        team.setName(request.getName().trim());
        team.setDescription(request.getDescription());

        Team saved = teamRepository.save(team);
        return mapToTeamResponse(saved);
    }

    @Transactional
    public void deleteTeam(UUID currentUserId, UUID workspaceId, UUID teamId) {
        checkOwnerOrAdmin(currentUserId, workspaceId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getWorkspace().getId().equals(workspaceId)) {
            throw new RuntimeException("Team does not belong to this workspace");
        }

        teamRepository.delete(team);
    }

    // --- CUSTOM ROLES CRUD ---

    @Transactional
    public CustomRoleResponse createCustomRole(UUID currentUserId, UUID workspaceId, CustomRoleRequest request) {
        checkOwnerOrAdmin(currentUserId, workspaceId);
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("WorkspaceEntity not found"));

        CustomRole role = CustomRole.builder()
                .workspace(workspace)
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        CustomRole saved = customRoleRepository.save(role);
        return mapToCustomRoleResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomRoleResponse> getCustomRoles(UUID currentUserId, UUID workspaceId) {
        verifyWorkspaceMember(currentUserId, workspaceId);
        return customRoleRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToCustomRoleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomRoleResponse updateCustomRole(UUID currentUserId, UUID workspaceId, UUID roleId, CustomRoleRequest request) {
        checkOwnerOrAdmin(currentUserId, workspaceId);
        CustomRole role = customRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Custom role not found"));

        if (!role.getWorkspace().getId().equals(workspaceId)) {
            throw new RuntimeException("Custom role does not belong to this workspace");
        }

        role.setName(request.getName().trim());
        role.setDescription(request.getDescription());

        CustomRole saved = customRoleRepository.save(role);
        return mapToCustomRoleResponse(saved);
    }

    @Transactional
    public void deleteCustomRole(UUID currentUserId, UUID workspaceId, UUID roleId) {
        checkOwnerOrAdmin(currentUserId, workspaceId);
        CustomRole role = customRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Custom role not found"));

        if (!role.getWorkspace().getId().equals(workspaceId)) {
            throw new RuntimeException("Custom role does not belong to this workspace");
        }

        customRoleRepository.delete(role);
    }

    // --- MEMBERS MANAGEMENT ---

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getMembers(UUID currentUserId, UUID workspaceId) {
        verifyWorkspaceMember(currentUserId, workspaceId);
        List<UserWorkspaceEntity> relations = userWorkspaceRepository.findByWorkspaceId(workspaceId);

        return relations.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMemberTeamsAndRoles(UUID currentUserId, UUID workspaceId, UUID memberUserId, UpdateMemberRolesTeamsRequest request) {
        UserWorkspaceEntity currentRel = userWorkspaceRepository.findByUserIdAndWorkspaceId(currentUserId, workspaceId)
                .orElseThrow(() -> new RuntimeException("Access denied: Not a member of this workspace"));

        if (currentRel.getRole() != WorkspaceRole.OWNER && currentRel.getRole() != WorkspaceRole.ADMIN) {
            throw new RuntimeException("Access denied: Only owners or admins can modify member assignments");
        }

        UserWorkspaceEntity memberRel = userWorkspaceRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId)
                .orElseThrow(() -> new RuntimeException("Member not found in this workspace"));

        // Enforce role security
        if (memberRel.getRole() == WorkspaceRole.OWNER && !currentUserId.equals(memberUserId)) {
            throw new RuntimeException("Access denied: Cannot modify the owner's role or assignments");
        }

        if (currentRel.getRole() == WorkspaceRole.ADMIN && memberRel.getRole() == WorkspaceRole.ADMIN && !currentUserId.equals(memberUserId)) {
            throw new RuntimeException("Access denied: Admins cannot modify other admins");
        }

        // Standard WorkspaceEntity Role can only be changed to OWNER by the OWNER
        if (request.getRole() != memberRel.getRole()) {
            if (request.getRole() == WorkspaceRole.OWNER && currentRel.getRole() != WorkspaceRole.OWNER) {
                throw new RuntimeException("Access denied: Only the owner can transfer ownership");
            }
            memberRel.setRole(request.getRole());
        }

        // Validate and update teams
        Set<Team> teams = new HashSet<>();
        if (request.getTeamIds() != null && !request.getTeamIds().isEmpty()) {
            List<Team> foundTeams = teamRepository.findAllById(request.getTeamIds());
            for (Team team : foundTeams) {
                if (!team.getWorkspace().getId().equals(workspaceId)) {
                    throw new RuntimeException("Team " + team.getName() + " does not belong to this workspace");
                }
                teams.add(team);
            }
        }
        memberRel.setTeams(teams);

        // Validate and update custom roles
        Set<CustomRole> customRoles = new HashSet<>();
        if (request.getCustomRoleIds() != null && !request.getCustomRoleIds().isEmpty()) {
            List<CustomRole> foundRoles = customRoleRepository.findAllById(request.getCustomRoleIds());
            for (CustomRole role : foundRoles) {
                if (!role.getWorkspace().getId().equals(workspaceId)) {
                    throw new RuntimeException("Custom role " + role.getName() + " does not belong to this workspace");
                }
                customRoles.add(role);
            }
        }
        memberRel.setCustomRoles(customRoles);

        userWorkspaceRepository.save(memberRel);
    }

    private TeamResponse mapToTeamResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .build();
    }

    private CustomRoleResponse mapToCustomRoleResponse(CustomRole role) {
        return CustomRoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }

    private WorkspaceMemberResponse mapToMemberResponse(UserWorkspaceEntity rel) {
        UserEntity user = rel.getUser();
        return WorkspaceMemberResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(rel.getRole())
                .status(rel.getStatus() != null ? rel.getStatus().name() : "ACTIVE")
                .joinedAt(rel.getJoinedAt())
                .teams(rel.getTeams().stream().map(this::mapToTeamResponse).collect(Collectors.toList()))
                .customRoles(rel.getCustomRoles().stream().map(this::mapToCustomRoleResponse).collect(Collectors.toList()))
                .build();
    }
}
