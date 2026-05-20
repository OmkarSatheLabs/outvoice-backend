package com.omkarsathe.outvoice.organization.dto;

import com.omkarsathe.outvoice.organization.OrgRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {
    @NotNull
    private OrgRole role;
}
