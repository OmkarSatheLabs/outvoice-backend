package com.omkarsathe.outvoice.workspace.customer.dto;

import com.omkarsathe.outvoice.workspace.customer.SearchQueryTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequestIdentityResolutionIdentityMatch {
    private IdentityMatchStatus status;
    private SearchQueryTypeEnum type;
    private UUID workspaceId;
    private UUID customerId;
}
