package com.omkarsathe.outvoice.workspace.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class SearchWorkspaceResponse {
        private UUID id;
        private String workspaceName;
        private UUID countryId;
        private UUID currencyId;
    }
