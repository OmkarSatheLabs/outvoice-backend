package com.omkarsathe.outvoice.auth.mfa.dto;

import jakarta.validation.constraints.NotBlank;

public record MfaResendRequest(
    @NotBlank(message = "MFA session token is required")
    String mfaToken
) {}
