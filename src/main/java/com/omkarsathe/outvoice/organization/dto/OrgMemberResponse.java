package com.omkarsathe.outvoice.organization.dto;

import com.omkarsathe.outvoice.organization.OrgRole;
import com.omkarsathe.outvoice.organization.OrgUserStatus;
import com.omkarsathe.outvoice.organization.Permission;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrgMemberResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String mobileNumber;
    private OrgRole role;
    private List<Permission> permissions;
    private OrgUserStatus status;
    private UUID invitedBy;
    private LocalDateTime joinedAt;
}
