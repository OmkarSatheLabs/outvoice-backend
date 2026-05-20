package com.omkarsathe.outvoice.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Body for POST /api/auth/select-org. */
@Getter
@Setter
public class SelectOrgRequest {

    @NotNull
    private UUID orgId;
}
