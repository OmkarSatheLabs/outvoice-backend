package com.omkarsathe.outvoice.workspace.user;

import com.omkarsathe.outvoice.phone.PhoneCodeResponse;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        PhoneCodeResponse phoneCode,
        String mobile
) {
}
