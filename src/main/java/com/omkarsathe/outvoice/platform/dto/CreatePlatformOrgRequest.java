package com.omkarsathe.outvoice.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Body for POST /api/platform/orgs — creates an org and assigns an owner. */
@Getter
@Setter
public class CreatePlatformOrgRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    /** Email or mobile of the user who will become the org's first Owner. */
    @NotBlank
    private String ownerIdentifier;

    /**
     * If true, a new user account is created when no user exists with ownerIdentifier.
     * The temporary password is auto-generated and should be changed on first login.
     */
    private boolean createOwnerIfNotExists;

    private String ownerFullName;
}
