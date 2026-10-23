package com.omkarsathe.outvoice.admin;

import java.util.UUID;

public record AdminWorkspaceMemberResponse(
        UUID userId,
        String fullName,
        String email,
        String phoneCode,
        String mobile
) {
}
