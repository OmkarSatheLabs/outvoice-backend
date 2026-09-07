package com.omkarsathe.outvoice.user.workspace.invite;

import com.omkarsathe.outvoice.common.validation.EmailOrMobileRequired;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@EmailOrMobileRequired
public record CreateInviteRequest(
        @Email
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        UUID phoneCodeId,

        String mobile
) {}
