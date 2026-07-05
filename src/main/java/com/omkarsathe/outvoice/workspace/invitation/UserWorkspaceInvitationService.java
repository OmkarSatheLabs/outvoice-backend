package com.omkarsathe.outvoice.workspace.invitation;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.country.CountryRepository;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
import com.omkarsathe.outvoice.security.JwtService;
import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.user.UserRepository;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
import com.omkarsathe.outvoice.workspace.*;
import com.omkarsathe.outvoice.workspace.dto.*;
import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
import com.omkarsathe.outvoice.workspace.role.CustomRole;
import com.omkarsathe.outvoice.workspace.role.CustomRoleRepository;
import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import com.omkarsathe.outvoice.workspace.team.Team;
import com.omkarsathe.outvoice.workspace.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserWorkspaceInvitationService {

    private final UserWorkspaceInvitationRepository userWorkspaceInvitationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final UserWorkspaceRepository userWorkspaceRepository;
    private final TeamRepository teamRepository;
    private final CustomRoleRepository customRoleRepository;
    private final PhoneCodeRepository phoneCodeRepository;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public List<WorkspaceInviteResponse> getUserInvites(UUID userId) {
        return userWorkspaceInvitationRepository.findByUserId(userId).stream()
                .map(uwi -> WorkspaceInviteResponse.builder()
                        .id(uwi.getId())
                        .workspace(WorkspaceResponse.builder()
                                .id(uwi.getWorkspace().getId())
                                .name(uwi.getWorkspace().getName())
                                .slug(uwi.getWorkspace().getSlug())
                                .role(uwi.getRole())
                                .isDefault(false)
                                .currencyId(uwi.getWorkspace().getCurrency() != null
                                        ? uwi.getWorkspace().getCurrency().getId() : null)
                                .countryId(uwi.getWorkspace().getCountry() != null
                                        ? uwi.getWorkspace().getCountry().getId() : null)
                                .build())
                        .invitedBy(uwi.getInvitedBy() != null
                                ? uwi.getInvitedBy().getFullName() : "System")
                        .status(uwi.getStatus())
                        .token(uwi.getToken())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkspaceInvitationResponse inviteUser(UUID currentUserId, UUID workspaceId, InviteUserRequest request) {
        // Verify current user is owner
        UserWorkspaceEntity currentRel = userWorkspaceRepository.findByUserIdAndWorkspaceId(currentUserId, workspaceId)
                .orElseThrow(() -> new RuntimeException("Access denied: Not a member of the workspace"));

        if (currentRel.getRole() != WorkspaceRole.OWNER) {
            throw new RuntimeException("Access denied: Only the workspace owner can invite users");
        }

        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("WorkspaceEntity not found"));

        // Validate invite request has email or mobile
        if (!StringUtils.hasText(request.getEmail()) && !StringUtils.hasText(request.getMobile())) {
            throw new RuntimeException("Email or mobile number is required to invite a user");
        }

        UserEntity user = null;

        if (StringUtils.hasText(request.getEmail())) {
            user = userRepository.findByEmail(request.getEmail()).orElse(null);
        }

        // Validate phone code if mobile is provided
        PhoneCode phoneCode = null;
        if (StringUtils.hasText(request.getMobile())) {
            if (request.getPhoneCodeId() == null) {
                throw new RuntimeException("Phone code is required when inviting by mobile number");
            }
            phoneCode = phoneCodeRepository.findById(request.getPhoneCodeId())
                    .orElseThrow(() -> new RuntimeException("Phone code not found"));
        }

        // Load and validate teams
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

        // Load and validate custom roles
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

        // Generate invitation
        String token = UUID.randomUUID().toString();
        UserEntity invitedByUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        UserWorkspaceInvitationEntity invitation = UserWorkspaceInvitationEntity.builder()
                .workspace(workspace)
                .user(user)
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null)
                .phoneCode(phoneCode)
                .mobile(StringUtils.hasText(request.getMobile()) ? request.getMobile().trim() : null)
                .role(request.getRole())
                .invitedBy(invitedByUser)
                .token(token)
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusDays(7)) // Valid for 7 days
                .teams(teams)
                .customRoles(customRoles)
                .build();

        UserWorkspaceInvitationEntity saved = userWorkspaceInvitationRepository.save(invitation);

        String joiningLink = "/join/" + workspace.getSlug() + "?token=" + token;

        return mapToResponse(saved, joiningLink);
    }

    @Transactional(readOnly = true)
    public WorkspaceInvitationResponse getInvitationByToken(String token) {
        UserWorkspaceInvitationEntity invitation = userWorkspaceInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new RuntimeException("Invitation is already " + invitation.getStatus().toLowerCase());
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }

        String joiningLink = "/join/" + invitation.getWorkspace().getSlug() + "?token=" + token;
        return mapToResponse(invitation, joiningLink);
    }

    @Transactional
    public void acceptInvitation(UUID currentUserId, String token) {
        UserWorkspaceInvitationEntity invitation = userWorkspaceInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new RuntimeException("Invitation is already " + invitation.getStatus().toLowerCase());
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("UserEntity not found"));

        // Validate matching email if specified in invite
        if (invitation.getEmail() != null && !invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("This invitation was sent to a different email address");
        }

        // Validate matching mobile if specified in invite
        if (invitation.getMobile() != null && !invitation.getMobile().equals(user.getMobile())) {
            throw new RuntimeException("This invitation was sent to a different mobile number");
        }

        Optional<UserWorkspaceEntity> existingRel = userWorkspaceRepository.findByUserIdAndWorkspaceId(user.getId(), invitation.getWorkspace().getId());
        if (existingRel.isPresent() && existingRel.get().getStatus() == MemberStatusEnum.ACTIVE) {
            throw new RuntimeException("You are already a member of this workspace");
        }

        // Determine if this should be the default workspace
        boolean hasActiveWorkspace = userWorkspaceRepository.findByUserIdAndStatusFetchWorkspace(user.getId(), MemberStatusEnum.ACTIVE).stream()
                .anyMatch(uw -> Boolean.TRUE.equals(uw.getIsDefaultWorkspace()));

        UserWorkspaceEntity relationship;
        if (existingRel.isPresent()) {
            relationship = existingRel.get();
            relationship.setRole(invitation.getRole());
            relationship.setStatus(MemberStatusEnum.ACTIVE);
            relationship.setIsDefaultWorkspace(!hasActiveWorkspace);
            relationship.setInvitedBy(invitation.getInvitedBy());
            relationship.setTeams(new HashSet<>(invitation.getTeams()));
            relationship.setCustomRoles(new HashSet<>(invitation.getCustomRoles()));
            relationship.setJoinedAt(LocalDateTime.now());
        } else {
            relationship = UserWorkspaceEntity.builder()
                    .user(user)
                    .workspace(invitation.getWorkspace())
                    .role(invitation.getRole())
                    .status(MemberStatusEnum.ACTIVE)
                    .isDefaultWorkspace(!hasActiveWorkspace)
                    .invitedBy(invitation.getInvitedBy())
                    .teams(new HashSet<>(invitation.getTeams()))
                    .customRoles(new HashSet<>(invitation.getCustomRoles()))
                    .joinedAt(LocalDateTime.now())
                    .build();
        }

        userWorkspaceRepository.save(relationship);
        invitation.setStatus("ACCEPTED");
        userWorkspaceInvitationRepository.save(invitation);
    }

    public void declineInvitation(UUID userId, UUID workspaceId, UUID invitationId, String token) {

        UserWorkspaceInvitationEntity invitation = userWorkspaceInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!invitation.getWorkspace().getId().equals(workspaceId)) {
            throw new IllegalArgumentException("Invitation does not belong to the specified workspace.");
        }

        if (invitation.getUser() != null &&
                !invitation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Invitation does not belong to the specified user.");
        }

        if (!invitation.getToken().equals(token)) {
            throw new IllegalArgumentException("Invalid invitation token.");
        }

        invitation.setStatus("DECLINED");

        userWorkspaceInvitationRepository.save(invitation);
    }

    @Transactional
    public AuthResponse signupAndAcceptInvitation(AcceptInvitationAndSignupRequest request) {
        UserWorkspaceInvitationEntity invitation = userWorkspaceInvitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new RuntimeException("Invitation is already " + invitation.getStatus().toLowerCase());
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation has expired");
        }

        // Use details from invitation to register
        String email = invitation.getEmail();
        String mobile = invitation.getMobile() != null ? invitation.getMobile() : request.getMobile();
        PhoneCode phoneCode = invitation.getPhoneCode() != null ? invitation.getPhoneCode() : 
                (request.getPhoneCodeId() != null ? phoneCodeRepository.findById(request.getPhoneCodeId()).orElse(null) : null);

        if (email == null && mobile == null) {
            throw new RuntimeException("Invalid invitation data: missing contact info");
        }

        // Verify uniqueness
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("An account with this email already exists");
        }

        if (mobile != null && phoneCode != null && userRepository.findByMobileAndPhoneCodeId(mobile, phoneCode.getId()).isPresent()) {
            throw new RuntimeException("An account with this mobile number already exists");
        }

        Country userCountry = countryRepository.findById(request.getUserCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found"));

        // Register user WITHOUT personal workspace
        UserEntity user = UserEntity.builder()
                .email(email)
                .mobile(mobile)
                .phoneCode(phoneCode)
                .fullName(request.getFullName().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isEmailVerified(false)
                .isMobileVerified(false)
                .isPlaceholder(false)
                .country(userCountry)
                .build();

        UserEntity savedUser = userRepository.save(user);

        // Add to workspace
        UserWorkspaceEntity relationship = UserWorkspaceEntity.builder()
                .user(savedUser)
                .workspace(invitation.getWorkspace())
                .role(invitation.getRole())
                .status(MemberStatusEnum.ACTIVE)
                .isDefaultWorkspace(true) // First workspace, so default
                .invitedBy(invitation.getInvitedBy())
                .teams(invitation.getTeams())
                .customRoles(invitation.getCustomRoles())
                .joinedAt(LocalDateTime.now())
                .build();

        userWorkspaceRepository.save(relationship);
        invitation.setStatus("ACCEPTED");
        userWorkspaceInvitationRepository.save(invitation);

        return new AuthResponse(jwtService.generateToken(savedUser.getUsername()));
    }

    @Transactional(readOnly = true)
    public List<WorkspaceInvitationResponse> getInvitations(UUID currentUserId, UUID workspaceId) {
        UserWorkspaceEntity currentRel = userWorkspaceRepository.findByUserIdAndWorkspaceId(currentUserId, workspaceId)
                .orElseThrow(() -> new RuntimeException("Access denied: Not a member of the workspace"));

        if (currentRel.getRole() != WorkspaceRole.OWNER && currentRel.getRole() != WorkspaceRole.ADMIN) {
            throw new RuntimeException("Access denied: Only owners or admins can view invitations");
        }

        return userWorkspaceInvitationRepository.findByWorkspaceIdAndStatusIn(workspaceId, List.of("PENDING", "DECLINED", "EXPIRED")).stream()
                .map(inv -> mapToResponse(inv, "/join/" + inv.getWorkspace().getSlug() + "?token=" + inv.getToken()))
                .collect(Collectors.toList());
    }

    private WorkspaceInvitationResponse mapToResponse(UserWorkspaceInvitationEntity invitation, String joiningLink) {
        return WorkspaceInvitationResponse.builder()
                .id(invitation.getId())
                .workspaceId(invitation.getWorkspace().getId())
                .workspaceName(invitation.getWorkspace().getName())
                .workspaceSlug(invitation.getWorkspace().getSlug())
                .email(invitation.getEmail())
                .mobile(invitation.getMobile())
                .role(invitation.getRole())
                .invitedBy(invitation.getInvitedBy() != null ? invitation.getInvitedBy().getFullName() : "System")
                .token(invitation.getToken())
                .status(invitation.getStatus())
                .joiningLink(joiningLink)
                .createdAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .teamIds(invitation.getTeams().stream().map(Team::getId).collect(Collectors.toSet()))
                .customRoleIds(invitation.getCustomRoles().stream().map(CustomRole::getId).collect(Collectors.toSet()))
                .build();
    }
}
