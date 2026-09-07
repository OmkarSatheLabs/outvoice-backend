package com.omkarsathe.outvoice.workspace.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequestIdentityResolution {
    private CreateCustomerRequestIdentityResolutionIdentityMatch identityMatch;
}
