package com.omkarsathe.outvoice.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LoginRequest {

    @Email
    private String email;

    private UUID phoneCodeId;

    private String mobile;

    @NotBlank
    private String password;
}
