package com.omkarsathe.outvoice.auth.dto;

import com.omkarsathe.outvoice.organization.OrgRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/** Lightweight org representation returned in multi-org login response for the org picker. */
@Getter
@AllArgsConstructor
public class OrgSummaryDto {
    private final UUID orgId;
    private final String name;
    private final String slug;
    private final OrgRole role;
}
