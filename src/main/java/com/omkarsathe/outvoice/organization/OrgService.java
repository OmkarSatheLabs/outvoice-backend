package com.omkarsathe.outvoice.organization;

import com.omkarsathe.outvoice.audit.AuditAction;
import com.omkarsathe.outvoice.audit.AuditLogService;
import com.omkarsathe.outvoice.common.exception.BusinessException;
import com.omkarsathe.outvoice.common.exception.NotFoundException;
import com.omkarsathe.outvoice.organization.dto.*;
import com.omkarsathe.outvoice.security.UserContext;
import com.omkarsathe.outvoice.user.User;
import com.omkarsathe.outvoice.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrgService {

    private final OrganizationRepository orgRepository;
    private final OrgUserRepository orgUserRepository;
    private final OrgInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    // ─── Queries ─────────────────────────────────────────────────────────────

    /** Returns all orgs the current user is an active member of. */
    @Transactional(readOnly = true)
    public List<OrgResponse> myOrgs(UserContext ctx) {
        return orgUserRepository.findByUserIdAndStatus(ctx.userId(), OrgUserStatus.ACTIVE).stream()
                .map(ou -> toOrgResponse(ou.getOrg()))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrgResponse getOrg(UUID orgId, UserContext ctx) {
        assertMember(orgId, ctx);
        return toOrgResponse(loadOrg(orgId));
    }

    @Transactional(readOnly = true)
    public List<OrgMemberResponse> listMembers(UUID orgId, UserContext ctx) {
        assertMember(orgId, ctx);
        return orgUserRepository.findByOrgId(orgId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PendingInviteResponse> listPendingApprovals(UUID orgId, UserContext ctx) {
        assertRole(ctx, OrgRole.OWNER);
        assertBelongsToOrg(orgId, ctx);
        return invitationRepository.findByOrgIdAndApprovalStatusAndStatus(
                orgId, InvitationApprovalStatus.PENDING, InvitationStatus.PENDING).stream()
                .map(this::toPendingInviteResponse)
                .toList();
    }

    // ─── Invite ───────────────────────────────────────────────────────────────

    /**
     * Invites a user to the org.
     * - MEMBER invite: immediately creates an INVITED org_user record and would send an email (email not wired yet).
     * - SUPER_USER invite: creates a PENDING_APPROVAL record; Owner must approve before the invite is sent.
     */
    @Transactional
    public PendingInviteResponse inviteMember(UUID orgId,
                                              InviteMemberRequest request,
                                              UserContext ctx,
                                              HttpServletRequest httpRequest) {
        assertBelongsToOrg(orgId, ctx);

        if (request.getIntendedRole() == OrgRole.OWNER) {
            throw new BusinessException("Cannot directly invite as OWNER. Use the ownership transfer flow.");
        }
        if (request.getInviteeEmail() == null && request.getInviteeMobile() == null) {
            throw new BusinessException("At least one of inviteeEmail or inviteeMobile is required");
        }

        // Super Users can only invite Members
        if (ctx.role() == OrgRole.SUPER_USER && request.getIntendedRole() != OrgRole.MEMBER) {
            throw new BusinessException("Super Users can only invite Members directly. Super User invitations require Owner approval.");
        }

        Organization org = loadOrg(orgId);
        User actor = loadUser(ctx.userId());

        InvitationApprovalStatus approvalStatus = request.getIntendedRole() == OrgRole.SUPER_USER
                ? InvitationApprovalStatus.PENDING
                : InvitationApprovalStatus.APPROVED;

        OrgInvitation invitation = invitationRepository.save(OrgInvitation.builder()
                .org(org)
                .invitedBy(actor)
                .inviteeEmail(request.getInviteeEmail())
                .inviteeMobile(request.getInviteeMobile())
                .intendedRole(request.getIntendedRole())
                .intendedPermissions(request.getIntendedPermissions())
                .approvalStatus(approvalStatus)
                .token(generateSecureToken())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(InvitationStatus.PENDING)
                .build());

        auditLogService.log(orgId, ctx.userId(), AuditAction.MEMBER_INVITED, null,
                null, Map.of("inviteeEmail", orEmpty(invitation.getInviteeEmail()),
                        "role", request.getIntendedRole().name()), httpRequest);

        return toPendingInviteResponse(invitation);
    }

    @Transactional
    public void approveInvite(UUID orgId, UUID invitationId, UserContext ctx, HttpServletRequest httpRequest) {
        assertRole(ctx, OrgRole.OWNER);
        assertBelongsToOrg(orgId, ctx);

        OrgInvitation inv = loadInvitation(invitationId);
        if (inv.getApprovalStatus() != InvitationApprovalStatus.PENDING) {
            throw new BusinessException("Invitation is not pending approval");
        }

        User approver = loadUser(ctx.userId());
        inv.setApprovalStatus(InvitationApprovalStatus.APPROVED);
        inv.setApprovedBy(approver);
        invitationRepository.save(inv);

        auditLogService.log(orgId, ctx.userId(), AuditAction.INVITE_APPROVED, null,
                null, Map.of("invitationId", invitationId.toString()), httpRequest);
    }

    @Transactional
    public void rejectInvite(UUID orgId, UUID invitationId, UserContext ctx, HttpServletRequest httpRequest) {
        assertRole(ctx, OrgRole.OWNER);
        assertBelongsToOrg(orgId, ctx);

        OrgInvitation inv = loadInvitation(invitationId);
        if (inv.getApprovalStatus() != InvitationApprovalStatus.PENDING) {
            throw new BusinessException("Invitation is not pending approval");
        }

        inv.setApprovalStatus(InvitationApprovalStatus.REJECTED);
        inv.setStatus(InvitationStatus.CANCELLED);
        invitationRepository.save(inv);

        auditLogService.log(orgId, ctx.userId(), AuditAction.INVITE_REJECTED, null,
                null, Map.of("invitationId", invitationId.toString()), httpRequest);
    }

    // ─── Permission / Role Updates ────────────────────────────────────────────

    @Transactional
    public OrgMemberResponse updatePermissions(UUID orgId,
                                               UUID targetUserId,
                                               UpdatePermissionsRequest request,
                                               UserContext ctx,
                                               HttpServletRequest httpRequest) {
        assertBelongsToOrg(orgId, ctx);

        OrgUser target = loadMembership(orgId, targetUserId);
        if (target.getRole() != OrgRole.MEMBER) {
            throw new BusinessException("Permissions can only be updated for MEMBER users");
        }

        // Super Users can update permissions for Members; Owner can always
        if (ctx.role() == OrgRole.MEMBER) {
            throw new BusinessException("Members cannot update permissions");
        }

        List<Permission> oldPerms = target.getPermissions();
        target.setPermissions(request.getPermissions());
        orgUserRepository.save(target);

        auditLogService.log(orgId, ctx.userId(), AuditAction.PERMISSIONS_CHANGED, targetUserId,
                Map.of("permissions", oldPerms != null ? oldPerms : List.of()),
                Map.of("permissions", request.getPermissions()), httpRequest);

        return toMemberResponse(target);
    }

    @Transactional
    public OrgMemberResponse updateRole(UUID orgId,
                                        UUID targetUserId,
                                        UpdateRoleRequest request,
                                        UserContext ctx,
                                        HttpServletRequest httpRequest) {
        // Only Owner can change roles
        assertRole(ctx, OrgRole.OWNER);
        assertBelongsToOrg(orgId, ctx);

        OrgUser target = loadMembership(orgId, targetUserId);
        if (target.getRole() == OrgRole.OWNER) {
            throw new BusinessException("Cannot change the Owner's role directly. Use the ownership transfer flow.");
        }
        if (request.getRole() == OrgRole.OWNER) {
            throw new BusinessException("Cannot promote to OWNER directly. Use the ownership transfer flow.");
        }

        OrgRole oldRole = target.getRole();
        target.setRole(request.getRole());
        // Wipe permissions when promoting to SUPER_USER
        if (request.getRole() == OrgRole.SUPER_USER) {
            target.setPermissions(null);
        }
        orgUserRepository.save(target);

        auditLogService.log(orgId, ctx.userId(), AuditAction.ROLE_CHANGED, targetUserId,
                Map.of("role", oldRole.name()), Map.of("role", request.getRole().name()), httpRequest);

        return toMemberResponse(target);
    }

    // ─── Suspend Member ───────────────────────────────────────────────────────

    @Transactional
    public void suspendMember(UUID orgId, UUID targetUserId, UserContext ctx, HttpServletRequest httpRequest) {
        assertBelongsToOrg(orgId, ctx);

        OrgUser target = loadMembership(orgId, targetUserId);
        if (target.getRole() == OrgRole.OWNER) {
            throw new BusinessException("The Owner cannot be suspended. Transfer ownership first.");
        }
        // Super Users cannot suspend other Super Users
        if (ctx.role() == OrgRole.SUPER_USER && target.getRole() == OrgRole.SUPER_USER) {
            throw new BusinessException("Super Users cannot suspend other Super Users");
        }
        if (ctx.role() == OrgRole.MEMBER) {
            throw new BusinessException("Members cannot suspend other members");
        }

        target.setStatus(OrgUserStatus.SUSPENDED);
        orgUserRepository.save(target);

        auditLogService.log(orgId, ctx.userId(), AuditAction.MEMBER_SUSPENDED, targetUserId,
                Map.of("status", "ACTIVE"), Map.of("status", "SUSPENDED"), httpRequest);
    }

    // ─── Transfer Ownership ───────────────────────────────────────────────────

    @Transactional
    public void transferOwnership(UUID orgId,
                                  TransferOwnershipRequest request,
                                  UserContext ctx,
                                  HttpServletRequest httpRequest) {
        assertRole(ctx, OrgRole.OWNER);
        assertBelongsToOrg(orgId, ctx);

        User currentOwnerUser = loadUser(ctx.userId());
        if (!passwordEncoder.matches(request.getPasswordConfirmation(), currentOwnerUser.getPasswordHash())) {
            throw new BusinessException("Password confirmation is incorrect");
        }

        OrgUser targetMembership = orgUserRepository
                .findActiveByOrgIdAndUserId(orgId, request.getTargetUserId())
                .orElseThrow(() -> new BusinessException("Target user is not an active member of this org"));

        if (targetMembership.getRole() != OrgRole.SUPER_USER) {
            throw new BusinessException("Ownership can only be transferred to an existing Super User of this org");
        }

        OrgUser currentOwnerMembership = orgUserRepository
                .findActiveByOrgIdAndUserId(orgId, ctx.userId())
                .orElseThrow(() -> new NotFoundException("Current owner membership not found"));

        // Atomic swap: current Owner → SUPER_USER, target → OWNER
        currentOwnerMembership.setRole(OrgRole.SUPER_USER);
        targetMembership.setRole(OrgRole.OWNER);
        orgUserRepository.saveAll(List.of(currentOwnerMembership, targetMembership));

        // Update denormalized owner_user_id on the org
        Organization org = loadOrg(orgId);
        org.setOwner(targetMembership.getUser());
        orgRepository.save(org);

        auditLogService.log(orgId, ctx.userId(), AuditAction.OWNERSHIP_TRANSFERRED,
                request.getTargetUserId(),
                Map.of("oldOwner", ctx.userId().toString()),
                Map.of("newOwner", request.getTargetUserId().toString()),
                httpRequest);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private OrgResponse toOrgResponse(Organization org) {
        return OrgResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .ownerUserId(org.getOwner() != null ? org.getOwner().getId() : null)
                .status(org.getStatus())
                .createdAt(org.getCreatedAt())
                .build();
    }

    private OrgMemberResponse toMemberResponse(OrgUser ou) {
        return OrgMemberResponse.builder()
                .userId(ou.getUser().getId())
                .fullName(ou.getUser().getFullName())
                .email(ou.getUser().getEmail())
                .mobileNumber(ou.getUser().getMobileNumber())
                .role(ou.getRole())
                .permissions(ou.getPermissions())
                .status(ou.getStatus())
                .invitedBy(ou.getInvitedBy() != null ? ou.getInvitedBy().getId() : null)
                .joinedAt(ou.getCreatedAt())
                .build();
    }

    private PendingInviteResponse toPendingInviteResponse(OrgInvitation inv) {
        return PendingInviteResponse.builder()
                .invitationId(inv.getId())
                .orgId(inv.getOrg().getId())
                .inviteeEmail(inv.getInviteeEmail())
                .inviteeMobile(inv.getInviteeMobile())
                .intendedRole(inv.getIntendedRole())
                .intendedPermissions(inv.getIntendedPermissions())
                .invitedByUserId(inv.getInvitedBy().getId())
                .createdAt(inv.getCreatedAt())
                .expiresAt(inv.getExpiresAt())
                .build();
    }

    private Organization loadOrg(UUID orgId) {
        return orgRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organisation not found: " + orgId));
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private OrgUser loadMembership(UUID orgId, UUID userId) {
        return orgUserRepository.findActiveByOrgIdAndUserId(orgId, userId)
                .orElseThrow(() -> new NotFoundException("User is not an active member of this org"));
    }

    private OrgInvitation loadInvitation(UUID invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation not found: " + invitationId));
    }

    private void assertMember(UUID orgId, UserContext ctx) {
        if (!orgUserRepository.existsByOrgIdAndUserIdAndStatus(orgId, ctx.userId(), OrgUserStatus.ACTIVE)) {
            throw new BusinessException("Access denied — you are not a member of this organisation");
        }
    }

    private void assertBelongsToOrg(UUID orgId, UserContext ctx) {
        if (ctx.orgId() == null || !ctx.orgId().equals(orgId)) {
            throw new BusinessException("Access denied — wrong org context in token");
        }
    }

    private void assertRole(UserContext ctx, OrgRole... roles) {
        for (OrgRole r : roles) {
            if (ctx.role() == r) return;
        }
        throw new BusinessException("Insufficient role for this operation");
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String orEmpty(String s) {
        return s != null ? s : "";
    }
}
