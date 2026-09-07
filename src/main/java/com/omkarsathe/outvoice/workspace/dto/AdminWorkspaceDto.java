package com.omkarsathe.outvoice.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWorkspaceDto {
    private UUID id;
    private String name;
    private String slug;
    private String ownerName;
    private String ownerEmail;
    private UUID countryId;
    private UUID currencyId;
    private List<String> features;
    private boolean suspended;
    private String createdAt;
    private int memberCount;
    private int invoiceCount;
    private BigDecimal totalBilled;
}
