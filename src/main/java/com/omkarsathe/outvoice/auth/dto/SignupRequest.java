package com.omkarsathe.outvoice.auth.dto;

import com.omkarsathe.outvoice.common.validation.EmailOrMobileRequired;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@EmailOrMobileRequired
public class SignupRequest {

    @Email
    private String email;

    private UUID phoneCodeId;

    private String mobile;

    @NotBlank
    private String fullName;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private UUID userCountryId;

    @NotBlank
    private String organizationName;

    @NotBlank
    private String organizationSlug;

    private UUID organizationCountryId;

    private UUID currencyId;

    // Defaults to fullName in the service if not provided
    private String taxComplianceName;

    private String panNumber;
    private String gstNumber;
    private String tanNumber;
}
