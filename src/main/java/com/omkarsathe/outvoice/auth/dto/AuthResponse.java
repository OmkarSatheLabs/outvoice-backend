package com.omkarsathe.outvoice.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private final String token;
    private final String tokenType;
    private final boolean mfaRequired;
    private final String mfaToken;
    private final String deliveryChannel;
    private final String maskedDestination;

    public AuthResponse(String token) {
        this(token, "Bearer", false, null, null, null);
    }

    public static AuthResponse ofToken(String token) {
        return new AuthResponse(token, "Bearer", false, null, null, null);
    }

    public static AuthResponse ofMfa(String mfaToken, String deliveryChannel, String maskedDestination) {
        return new AuthResponse(null, null, true, mfaToken, deliveryChannel, maskedDestination);
    }
}
