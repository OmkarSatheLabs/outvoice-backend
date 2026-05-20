package com.omkarsathe.outvoice.organization;

import com.omkarsathe.outvoice.audit.AuditLog;
import com.omkarsathe.outvoice.audit.AuditLogRepository;
import com.omkarsathe.outvoice.organization.dto.*;
import com.omkarsathe.outvoice.security.CurrentUser;
import com.omkarsathe.outvoice.security.RequiresRole;
import com.omkarsathe.outvoice.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orgs")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;
    private final AuditLogRepository auditLogRepository;

    /** All orgs the authenticated user belongs to. */
    @GetMapping("/mine")
    public List<OrgResponse> myOrgs(@CurrentUser UserContext ctx) {
        return orgService.myOrgs(ctx);
    }

    /** Details of a specific org (caller must be a member). */
    @GetMapping("/{orgId}")
    public OrgResponse getOrg(@PathVariable UUID orgId, @CurrentUser UserContext ctx) {
        return orgService.getOrg(orgId, ctx);
    }

    /** All members (with roles and permissions) of the org. */
    @GetMapping("/{orgId}/members")
    public List<OrgMemberResponse> listMembers(@PathVariable UUID orgId, @CurrentUser UserContext ctx) {
        return orgService.listMembers(orgId, ctx);
    }

    /**
     * Invite a user.
     * - MEMBER invite → direct, no approval needed.
     * - SUPER_USER invite → requires Owner approval.
     */
    @PostMapping("/{orgId}/members/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public PendingInviteResponse inviteMember(@PathVariable UUID orgId,
                                              @Valid @RequestBody InviteMemberRequest request,
                                              @CurrentUser UserContext ctx,
                                              HttpServletRequest httpRequest) {
        return orgService.inviteMember(orgId, request, ctx, httpRequest);
    }

    /** Owner approves a pending Super User invitation. */
    @PostMapping("/{orgId}/invitations/{invitationId}/approve")
    @RequiresRole(OrgRole.OWNER)
    public void approveInvite(@PathVariable UUID orgId,
                              @PathVariable UUID invitationId,
                              @CurrentUser UserContext ctx,
                              HttpServletRequest httpRequest) {
        orgService.approveInvite(orgId, invitationId, ctx, httpRequest);
    }

    /** Owner rejects a pending Super User invitation. */
    @PostMapping("/{orgId}/invitations/{invitationId}/reject")
    @RequiresRole(OrgRole.OWNER)
    public void rejectInvite(@PathVariable UUID orgId,
                             @PathVariable UUID invitationId,
                             @CurrentUser UserContext ctx,
                             HttpServletRequest httpRequest) {
        orgService.rejectInvite(orgId, invitationId, ctx, httpRequest);
    }

    /** Pending Super User invitations awaiting Owner approval. */
    @GetMapping("/{orgId}/invitations/pending")
    @RequiresRole(OrgRole.OWNER)
    public List<PendingInviteResponse> pendingApprovals(@PathVariable UUID orgId,
                                                        @CurrentUser UserContext ctx) {
        return orgService.listPendingApprovals(orgId, ctx);
    }

    /** Update a Member's permissions (Owner or Super User). */
    @PatchMapping("/{orgId}/members/{userId}/permissions")
    public OrgMemberResponse updatePermissions(@PathVariable UUID orgId,
                                               @PathVariable UUID userId,
                                               @Valid @RequestBody UpdatePermissionsRequest request,
                                               @CurrentUser UserContext ctx,
                                               HttpServletRequest httpRequest) {
        return orgService.updatePermissions(orgId, userId, request, ctx, httpRequest);
    }

    /** Promote/demote a member's role (Owner only). */
    @PatchMapping("/{orgId}/members/{userId}/role")
    @RequiresRole(OrgRole.OWNER)
    public OrgMemberResponse updateRole(@PathVariable UUID orgId,
                                        @PathVariable UUID userId,
                                        @Valid @RequestBody UpdateRoleRequest request,
                                        @CurrentUser UserContext ctx,
                                        HttpServletRequest httpRequest) {
        return orgService.updateRole(orgId, userId, request, ctx, httpRequest);
    }

    /** Suspend (soft-delete) a member. Cannot suspend the Owner. */
    @DeleteMapping("/{orgId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspendMember(@PathVariable UUID orgId,
                              @PathVariable UUID userId,
                              @CurrentUser UserContext ctx,
                              HttpServletRequest httpRequest) {
        orgService.suspendMember(orgId, userId, ctx, httpRequest);
    }

    /** Transfer ownership to an existing Super User of the org. Requires password re-confirmation. */
    @PostMapping("/{orgId}/transfer")
    @RequiresRole(OrgRole.OWNER)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferOwnership(@PathVariable UUID orgId,
                                  @Valid @RequestBody TransferOwnershipRequest request,
                                  @CurrentUser UserContext ctx,
                                  HttpServletRequest httpRequest) {
        orgService.transferOwnership(orgId, request, ctx, httpRequest);
    }

    /** Paginated audit log for an org — Owner only. */
    @GetMapping("/{orgId}/audit-log")
    @RequiresRole(OrgRole.OWNER)
    public Page<AuditLog> auditLog(@PathVariable UUID orgId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size,
                                   @CurrentUser UserContext ctx) {
        return auditLogRepository.findByOrgId(orgId, PageRequest.of(page, size));
    }
}
