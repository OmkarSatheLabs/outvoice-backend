package com.omkarsathe.outvoice.organization;

/** Lifecycle status of a user's membership record in an org. */
public enum OrgUserStatus {
    /** Fully active member. */
    ACTIVE,
    /** Invite sent; user has not accepted yet. */
    INVITED,
    /** Super-User invite awaiting Owner approval before being sent. */
    PENDING_APPROVAL,
    /** Member was removed (soft-deleted). */
    SUSPENDED
}
