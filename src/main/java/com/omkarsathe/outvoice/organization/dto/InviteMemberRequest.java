package com.omkarsathe.outvoice.organization.dto;

import com.omkarsathe.outvoice.organization.OrgRole;
import com.omkarsathe.outvoice.organization.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InviteMemberRequest {

    /** Email of the invitee (one of email/mobile required). */
    private String inviteeEmail;

    /** Mobile of the invitee (one of email/mobile required). */
    private String inviteeMobile;

    @NotNull
    private OrgRole intendedRole; // only SUPER_USER or MEMBER are valid here

    /** Required when intendedRole = MEMBER. */
    private List<Permission> intendedPermissions;
}
