package com.omkarsathe.outvoice.organization.dto;

import com.omkarsathe.outvoice.organization.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdatePermissionsRequest {
    @NotNull
    private List<Permission> permissions;
}
