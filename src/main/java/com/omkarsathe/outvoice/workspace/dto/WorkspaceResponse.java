package com.omkarsathe.outvoice.workspace.dto;

import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    private UUID id;
    private String name;
    private String slug;
    private WorkspaceRole role;
    private boolean isDefault;
    private UUID currencyId;
    private UUID countryId;
}
