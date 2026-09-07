package com.omkarsathe.outvoice.auth.passwordreset;

public record ResetPasswordRequest(
        String token,
        String newPassword
) {
}
