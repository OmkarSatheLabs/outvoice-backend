package com.omkarsathe.outvoice.audit;

/** All auditable events in the system. */
public enum AuditAction {
    ORG_CREATED,
    MEMBER_INVITED,
    INVITE_APPROVED,
    INVITE_REJECTED,
    INVITE_ACCEPTED,
    ROLE_CHANGED,
    PERMISSIONS_CHANGED,
    MEMBER_SUSPENDED,
    OWNERSHIP_TRANSFERRED
}
