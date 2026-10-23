package com.omkarsathe.outvoice.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminUpdateWorkspaceRequest(
        @NotBlank(message = "Workspace name cannot be empty")
        String name,
        String invoiceNumberPrefix
) {
}
