package com.omkarsathe.outvoice.workspace.customer.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CreateWorkspaceCustomerResponseDto {
    private UUID id;
    private UUID workspaceId;
    private String displayName;
    private String email;
    private String mobile;
    private UUID linkedWorkspaceId;
    private String defaultCurrencyId;
}
