package com.omkarsathe.outvoice.workspace;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.country.CountryRepository;
import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
import com.omkarsathe.outvoice.security.JwtService;
import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.user.UserRepository;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
import com.omkarsathe.outvoice.workspace.dto.AcceptInvitationAndSignupRequest;
import com.omkarsathe.outvoice.workspace.dto.InviteUserRequest;
import com.omkarsathe.outvoice.workspace.dto.WorkspaceInvitationResponse;
import com.omkarsathe.outvoice.workspace.invitation.UserWorkspaceInvitationEntity;
import com.omkarsathe.outvoice.workspace.invitation.UserWorkspaceInvitationRepository;
import com.omkarsathe.outvoice.workspace.invitation.UserWorkspaceInvitationService;
import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
import com.omkarsathe.outvoice.workspace.role.CustomRoleRepository;
import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import com.omkarsathe.outvoice.workspace.team.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceInvitationServiceTest {

    @Mock
    private UserWorkspaceInvitationRepository workspaceInvitationRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserWorkspaceRepository userWorkspaceRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private CustomRoleRepository customRoleRepository;
    @Mock
    private PhoneCodeRepository phoneCodeRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserWorkspaceInvitationService workspaceInvitationService;

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
        workspace = WorkspaceEntity.builder().id(workspaceId).name("Test WorkspaceEntity").slug("test-ws").build();

        ownerRel = UserWorkspaceEntity.builder()
                .user(owner)
                .workspace(workspace)
                .role(WorkspaceRole.OWNER)
                .status(MemberStatusEnum.ACTIVE)
                .build();
    }

    @Test
    void inviteUser_shouldFail_whenInvitingUserIsNotOwner() {
        UUID otherUserId = UUID.randomUUID();
        UserWorkspaceEntity otherRel = UserWorkspaceEntity.builder()
                .user(UserEntity.builder().id(otherUserId).build())
                .workspace(workspace)
                .role(WorkspaceRole.MEMBER)
                .status(MemberStatusEnum.ACTIVE)
                .build();

        when(userWorkspaceRepository.findByUserIdAndWorkspaceId(otherUserId, workspaceId))
                .thenReturn(Optional.of(otherRel));

        InviteUserRequest request = InviteUserRequest.builder()
                .email("invitee@test.com")
                .role(WorkspaceRole.MEMBER)
                .build();

        assertThatThrownBy(() -> workspaceInvitationService.inviteUser(otherUserId, workspaceId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only the workspace owner can invite users");
    }

    @Test
    void inviteUser_shouldCreateInvitation_whenSuccessful() {
        when(userWorkspaceRepository.findByUserIdAndWorkspaceId(ownerId, workspaceId))
                .thenReturn(Optional.of(ownerRel));
        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(workspace));
        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        InviteUserRequest request = InviteUserRequest.builder()
                .email("invitee@test.com")
                .role(WorkspaceRole.ADMIN)
                .teamIds(Collections.emptySet())
                .customRoleIds(Collections.emptySet())
                .build();

        when(workspaceInvitationRepository.save(any(UserWorkspaceInvitationEntity.class)))
                .thenAnswer(invocation -> {
                    UserWorkspaceInvitationEntity invite = invocation.getArgument(0);
                    invite.setId(UUID.randomUUID());
                    invite.setCreatedAt(LocalDateTime.now());
                    return invite;
                });

        WorkspaceInvitationResponse response = workspaceInvitationService.inviteUser(ownerId, workspaceId, request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("invitee@test.com");
        assertThat(response.getRole()).isEqualTo(WorkspaceRole.ADMIN);
        assertThat(response.getJoiningLink()).contains("test-ws?token=");
        verify(workspaceInvitationRepository, times(1)).save(any(UserWorkspaceInvitationEntity.class));
    }

    @Test
    void acceptInvitation_shouldCreateWorkspaceRelation_forExistingUser() {
        String token = "valid-token";
        UserEntity invitee = UserEntity.builder().id(UUID.randomUUID()).email("invitee@test.com").build();
        UserWorkspaceInvitationEntity invite = UserWorkspaceInvitationEntity.builder()
                .id(UUID.randomUUID())
                .workspace(workspace)
                .email("invitee@test.com")
                .role(WorkspaceRole.MEMBER)
                .invitedBy(owner)
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusDays(2))
                .teams(Collections.emptySet())
                .customRoles(Collections.emptySet())
                .build();

        when(workspaceInvitationRepository.findByToken(token)).thenReturn(Optional.of(invite));
        when(userRepository.findById(invitee.getId())).thenReturn(Optional.of(invitee));
        when(userWorkspaceRepository.findByUserIdAndWorkspaceId(invitee.getId(), workspaceId))
                .thenReturn(Optional.empty());

        workspaceInvitationService.acceptInvitation(invitee.getId(), token);

        ArgumentCaptor<UserWorkspaceEntity> relationCaptor = ArgumentCaptor.forClass(UserWorkspaceEntity.class);
        verify(userWorkspaceRepository).save(relationCaptor.capture());

        UserWorkspaceEntity savedRel = relationCaptor.getValue();
        assertThat(savedRel.getUser().getId()).isEqualTo(invitee.getId());
        assertThat(savedRel.getWorkspace().getId()).isEqualTo(workspaceId);
        assertThat(savedRel.getRole()).isEqualTo(WorkspaceRole.MEMBER);
        assertThat(savedRel.getStatus()).isEqualTo(MemberStatusEnum.ACTIVE);

        assertThat(invite.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    void signupAndAcceptInvitation_shouldRegisterNewUser_withoutCreatingPersonalWorkspace() {
        String token = "valid-token";
        UserWorkspaceInvitationEntity invite = UserWorkspaceInvitationEntity.builder()
                .id(UUID.randomUUID())
                .workspace(workspace)
                .email("invitee@test.com")
                .role(WorkspaceRole.MEMBER)
                .invitedBy(owner)
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusDays(2))
                .teams(Collections.emptySet())
                .customRoles(Collections.emptySet())
                .build();

        UUID countryId = UUID.randomUUID();
        Country country = Country.builder().id(countryId).name("India").build();

        AcceptInvitationAndSignupRequest request = AcceptInvitationAndSignupRequest.builder()
                .token(token)
                .fullName("New Invitee")
                .password("password123")
                .userCountryId(countryId)
                .build();

        when(workspaceInvitationRepository.findByToken(token)).thenReturn(Optional.of(invite));
        when(userRepository.findByEmail("invitee@test.com")).thenReturn(Optional.empty());
        when(countryRepository.findById(countryId)).thenReturn(Optional.of(country));
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pass");

        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateToken(any())).thenReturn("jwt-token-xyz");

        AuthResponse authResponse = workspaceInvitationService.signupAndAcceptInvitation(request);

        assertThat(authResponse).isNotNull();
        assertThat(authResponse.getToken()).isEqualTo("jwt-token-xyz");

        // Verify that a UserEntity was saved
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("invitee@test.com");
        assertThat(savedUser.getFullName()).isEqualTo("New Invitee");

        // Verify that a UserWorkspaceEntity was saved
        ArgumentCaptor<UserWorkspaceEntity> relationCaptor = ArgumentCaptor.forClass(UserWorkspaceEntity.class);
        verify(userWorkspaceRepository).save(relationCaptor.capture());
        UserWorkspaceEntity savedRel = relationCaptor.getValue();
        assertThat(savedRel.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(savedRel.getWorkspace().getId()).isEqualTo(workspaceId);
        assertThat(savedRel.getRole()).isEqualTo(WorkspaceRole.MEMBER);
        assertThat(savedRel.getStatus()).isEqualTo(MemberStatusEnum.ACTIVE);

        // Verify no workspaceRepository.save() calls are made (which would indicate personal workspace creation)
        verifyNoInteractions(workspaceRepository);
    }
}
