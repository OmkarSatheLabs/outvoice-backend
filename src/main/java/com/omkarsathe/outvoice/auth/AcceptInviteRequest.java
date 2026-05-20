package com.omkarsathe.outvoice.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Body for POST /api/auth/invite/accept. */
@Getter
@Setter
public class AcceptInviteRequest {

    @NotBlank
    private String token;

    /** Required only when the invitee doesn't have an account yet — creates one on accept. */
    private String fullName;

    /** Password required only when creating a new account. */
    private String password;
}
