package com.omkarsathe.outvoice.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Body for POST /api/platform/auth/login. */
@Getter
@Setter
public class PlatformLoginRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
