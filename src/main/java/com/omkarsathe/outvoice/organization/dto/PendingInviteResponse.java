package com.omkarsathe.outvoice.organization.dto;

import com.omkarsathe.outvoice.organization.OrgRole;
import com.omkarsathe.outvoice.organization.Permission;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class PendingInviteResponse {
    private UUID invitationId;
    private UUID orgId;
    private String inviteeEmail;
    private String inviteeMobile;
    private OrgRole intendedRole;
    private List<Permission> intendedPermissions;
    private UUID invitedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
