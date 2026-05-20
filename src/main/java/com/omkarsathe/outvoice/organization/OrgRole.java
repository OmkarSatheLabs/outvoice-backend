package com.omkarsathe.outvoice.organization;

/** Role a user holds within a specific organisation. OWNER is a flag on a SUPER_USER — not a separate entity type. */
public enum OrgRole {
    OWNER,
    SUPER_USER,
    MEMBER
}
