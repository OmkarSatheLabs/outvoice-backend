package com.omkarsathe.outvoice.workspace.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchEntityResponse {
    private UUID id;
//    private String name;
//    private String type;
    private String customerName;
    private List<SearchWorkspaceResponse> workspaces;
    private String email;
    private UUID phoneCodeId;
    private String mobile;
}

