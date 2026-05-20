package com.omkarsathe.outvoice.security;

import com.omkarsathe.outvoice.organization.OrgRole;
import com.omkarsathe.outvoice.organization.Permission;

import java.util.List;
import java.util.UUID;

/**
 * Immutable snapshot of the authenticated user's identity + org context, extracted from the JWT.
 * Stored as the principal of the SecurityContext Authentication object.
 */
public record UserContext(
        UUID userId,
        /** Null when the token has no org context (user-level JWT, before /auth/select-org). */
        UUID orgId,
        /** Null when orgId is null. */
        OrgRole role,
        /** Empty for OWNER/SUPER_USER (they have all permissions); populated for MEMBER. */
        List<Permission> permissions
) {
    public boolean hasOrgContext() {
        return orgId != null && role != null;
    }

    public boolean hasPermission(Permission required) {
        if (!hasOrgContext()) return false;
        if (role == OrgRole.OWNER || role == OrgRole.SUPER_USER) return true;
        return permissions != null && permissions.contains(required);
    }

    public boolean hasRole(OrgRole required) {
        return role == required;
    }

    public boolean hasAnyRole(OrgRole... roles) {
        for (OrgRole r : roles) {
            if (r == role) return true;
        }
        return false;
    }
}
