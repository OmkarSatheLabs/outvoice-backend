package com.omkarsathe.outvoice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Returned by POST /auth/login.
 * If orgs is non-null the caller must choose an org via POST /auth/select-org.
 * If orgs is null a fully scoped token was issued immediately (single-org case).
 */
@Getter
@AllArgsConstructor
public class AuthResponse {
    private final String token;
    private final String tokenType = "Bearer";
    /** Non-null when the user belongs to multiple orgs — client must display org picker. */
    private final List<OrgSummaryDto> orgs;

    /** Convenience constructor for single-org / org-scoped token responses. */
    public AuthResponse(String token) {
        this.token = token;
        this.orgs = null;
    }
}
