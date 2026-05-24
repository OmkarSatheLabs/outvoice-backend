package com.omkarsathe.outvoice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private final String token;
    private final String tokenType = "Bearer";
//    private final String[] orgs;
//    private final String org;
}
