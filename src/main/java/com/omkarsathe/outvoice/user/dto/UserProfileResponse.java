package com.omkarsathe.outvoice.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String fullName;
    private String email;
    private String mobile;
    private UUID phoneCodeId;
    private String avatarUrl;
    private UUID userCountryId;
}
