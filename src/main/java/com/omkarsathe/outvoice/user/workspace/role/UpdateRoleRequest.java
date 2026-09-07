package com.omkarsathe.outvoice.user.workspace.role;

import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
        @Size(max = 100, message = "Role name must be at most 100 characters")
        String name,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description
) {}
