package com.omkarsathe.outvoice.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@ValidSignupContact
public record LoginRequest(
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @Size(min = 1, max = 4, message = "Phone code must be between 1 and 4 characters")
    String phoneCode,

    @Size(min = 7, max = 15, message = "Mobile number must be between 7 and 15 characters")
    String mobile,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    String password
) implements ContactRequest {}
