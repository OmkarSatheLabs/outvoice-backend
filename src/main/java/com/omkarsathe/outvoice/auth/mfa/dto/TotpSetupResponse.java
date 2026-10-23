package com.omkarsathe.outvoice.auth.mfa.dto;

public record TotpSetupResponse(
    String secret,
    String otpAuthUri
) {}
