package com.omkarsathe.outvoice.auth.passwordreset;

public record ForgotPasswordResponse(
        String message,
        String email
) {}
