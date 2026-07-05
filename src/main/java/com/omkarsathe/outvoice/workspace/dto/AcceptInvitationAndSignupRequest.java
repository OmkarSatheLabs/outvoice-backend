package com.omkarsathe.outvoice.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInvitationAndSignupRequest {
    @NotBlank(message = "Invitation token is required")
    private String token;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Country ID is required")
    private UUID userCountryId;

    private String mobile;
    private UUID phoneCodeId;
}
