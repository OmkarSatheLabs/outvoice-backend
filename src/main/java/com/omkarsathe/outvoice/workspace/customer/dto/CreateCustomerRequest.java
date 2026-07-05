package com.omkarsathe.outvoice.workspace.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    private String customerName;

    private String companyName;

    @Email(message = "Enter a valid email address")
    private String email;

    private UUID phoneCodeId;

    private String mobile;

    private UUID countryId;

    private UUID currencyId;

    private UUID userId;

    private UUID workspaceId;
}
