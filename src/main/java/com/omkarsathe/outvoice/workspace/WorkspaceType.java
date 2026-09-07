package com.omkarsathe.outvoice.workspace;

public enum WorkspaceType {
    STANDARD,       // normal user/business workspace
    PLATFORM_ADMIN, // the "outvoice" super-admin workspace
    SHADOW          // unclaimed, created via WorkspaceCustomerEntity linkage
}
