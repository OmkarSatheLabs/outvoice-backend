package com.omkarsathe.outvoice.workspace.user;

import com.omkarsathe.outvoice.phone.PhoneCodeResponse;

import java.util.UUID;

public record UserProfileResponse(
        String fullName,
        String email,
        String mobile,
        PhoneCodeResponse phoneCode,
        String avatarUrl,
        UUID userCountryId
) {
}
