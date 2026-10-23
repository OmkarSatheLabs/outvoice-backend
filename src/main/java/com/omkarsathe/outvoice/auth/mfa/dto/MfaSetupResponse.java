package com.omkarsathe.outvoice.auth.mfa.dto;

public record MfaSetupResponse(
    String sessionToken,
    String deliveryChannel,
    String maskedDestination
) {}
