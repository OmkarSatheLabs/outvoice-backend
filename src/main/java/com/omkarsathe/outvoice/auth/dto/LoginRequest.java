package com.omkarsathe.outvoice.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank
    private String identifier; // email or mobile number

    @NotBlank
    private String password;
}
