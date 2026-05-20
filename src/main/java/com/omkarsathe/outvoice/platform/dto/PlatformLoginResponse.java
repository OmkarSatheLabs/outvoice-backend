package com.omkarsathe.outvoice.platform.dto;

import com.omkarsathe.outvoice.platform.PlatformRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlatformLoginResponse {
    private String token;
    private final String tokenType = "Bearer";
    private UUID platformUserId;
    private PlatformRole role;
}
