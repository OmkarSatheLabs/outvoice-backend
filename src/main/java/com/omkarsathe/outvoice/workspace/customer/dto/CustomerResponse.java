package com.omkarsathe.outvoice.workspace.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private final UUID id;
    private final String name;
    private final String email;
    private final UUID phoneCodeId;
    private final String phoneCode;
    private final String mobile;
    private final String companyName;
    private final String billingAddress;
    private final String taxNumber;
}
