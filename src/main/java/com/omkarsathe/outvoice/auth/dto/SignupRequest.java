package com.omkarsathe.outvoice.auth.dto;

import com.omkarsathe.outvoice.common.validation.EmailOrMobileRequired;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// TODO: CONFLICT — V1 SignupRequest included organizationName and tax fields (pan/gst/tan).
//  The new design creates only a User; org creation is platform-admin-only.
//  Those fields have been removed. Existing clients sending them will have the extra
//  fields silently ignored by Jackson.
@Getter
@Setter
@EmailOrMobileRequired
public class SignupRequest {

    private String fullName;

    @Email
    private String email;

    private String mobileNumber;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
