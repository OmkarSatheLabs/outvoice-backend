package com.omkarsathe.outvoice.workspace;

import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
import com.omkarsathe.outvoice.workspace.dto.*;
import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
import com.omkarsathe.outvoice.workspace.member.UserWorkspaceMemberService;
import com.omkarsathe.outvoice.workspace.role.CustomRoleRepository;
import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import com.omkarsathe.outvoice.workspace.team.Team;
import com.omkarsathe.outvoice.workspace.team.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceMemberServiceTest {

    @Mock
    private UserWorkspaceRepository userWorkspaceRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private CustomRoleRepository customRoleRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private UserWorkspaceMemberService workspaceMemberService;

    private UUID ownerId;
    private UUID workspaceId;
    private UserEntity owner;
    private WorkspaceEntity workspace;
    private UserWorkspaceEntity ownerRel;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();

        owner = UserEntity.builder().id(ownerId).fullName("WorkspaceEntity Owner").email("owner@test.com").build();
        workspace = WorkspaceEntity.builder().id(workspaceId).name("Test WorkspaceEntity").build();

        ownerRel = UserWorkspaceEntity.builder()
                .user(owner)
                .workspace(workspace)
                .role(WorkspaceRole.OWNER)
                .status(MemberStatusEnum.ACTIVE)
                .build();
    }

    @Test
    void createTeam_shouldCreateAndReturnTeam_whenUserIsOwner() {
        when(userWorkspaceRepository.findByUserIdAndWorkspaceId(ownerId, workspaceId))
                .thenReturn(Optional.of(ownerRel));
        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(workspace));

        TeamRequest request = TeamRequest.builder()
                .name("Engineering")
                .description("Software developers")
                .build();

        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            team.setId(UUID.randomUUID());
            return team;
        });

        TeamResponse response = workspaceMemberService.createTeam(ownerId, workspaceId, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Engineering");
        assertThat(response.getDescription()).isEqualTo("Software developers");
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    void createTeam_shouldFail_whenUserIsMember() {
        UUID memberId = UUID.randomUUID();
        UserWorkspaceEntity memberRel = UserWorkspaceEntity.builder()
                .user(UserEntity.builder().id(memberId).build())
                .workspace(workspace)
                .role(WorkspaceRole.MEMBER)
                .status(MemberStatusEnum.ACTIVE)
                .build();

        when(userWorkspaceRepository.findByUserIdAndWorkspaceId(memberId, workspaceId))
                .thenReturn(Optional.of(memberRel));

        TeamRequest request = TeamRequest.builder().name("QA").build();

        assertThatThrownBy(() -> workspaceMemberService.createTeam(memberId, workspaceId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only owners or admins can perform this action");
    }

    @Test
    void updateMemberTeamsAndRoles_shouldApplyChanges_whenUserIsOwner() {
        UUID memberUserId = UUID.randomUUID();
        UserEntity memberUser = UserEntity.builder().id(memberUserId).fullName("Member UserEntity").build();
        UserWorkspaceEntity memberRel = UserWorkspaceEntity.builder()
                .user(memberUser)
                .workspace(workspace)
                .role(WorkspaceRole.MEMBER)
                .status(MemberStatusEnum.ACTIVE)
                .teams(new HashSet<>())
                .customRoles(new HashSet<>())
                .build();

        when(userWorkspaceRepository.findByUserIdAndWorkspaceId(ownerId, workspaceId))
                .thenReturn(Optional.of(ownerRel));
        when(userWorkspaceRepository.findByUserIdAndWorkspaceId(memberUserId, workspaceId))
                .thenReturn(Optional.of(memberRel));

        UUID teamId = UUID.randomUUID();
        Team team = Team.builder().id(teamId).workspace(workspace).name("Marketing").build();
        when(teamRepository.findAllById(any())).thenReturn(List.of(team));

        UpdateMemberRolesTeamsRequest request = UpdateMemberRolesTeamsRequest.builder()
                .role(WorkspaceRole.ADMIN)
                .teamIds(Set.of(teamId))
                .customRoleIds(Collections.emptySet())
                .build();

        workspaceMemberService.updateMemberTeamsAndRoles(ownerId, workspaceId, memberUserId, request);

        verify(userWorkspaceRepository, times(1)).save(memberRel);
        assertThat(memberRel.getRole()).isEqualTo(WorkspaceRole.ADMIN);
        assertThat(memberRel.getTeams()).contains(team);
    }

    @Test
    void updateMemberTeamsAndRoles_shouldFail_whenAdminAttemptsToModifyOwner() {
        UUID adminUserId = UUID.randomUUID();
        UserWorkspaceEntity adminRel = UserWorkspaceEntity.builder()
                .user(UserEntity.builder().id(adminUserId).build())
                .workspace(workspace)
                .role(WorkspaceRole.ADMIN)
                .status(MemberStatusEnum.ACTIVE)
                .build();

        when(userWorkspaceRepository.findByUserIdAndWorkspaceId(adminUserId, workspaceId))
                .thenReturn(Optional.of(adminRel));
        when(userWorkspaceRepository.findByUserIdAndWorkspaceId(ownerId, workspaceId))
                .thenReturn(Optional.of(ownerRel));

        UpdateMemberRolesTeamsRequest request = UpdateMemberRolesTeamsRequest.builder()
                .role(WorkspaceRole.MEMBER)
                .teamIds(Collections.emptySet())
                .customRoleIds(Collections.emptySet())
                .build();

        assertThatThrownBy(() -> workspaceMemberService.updateMemberTeamsAndRoles(adminUserId, workspaceId, ownerId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot modify the owner's role or assignments");
    }
}
