package com.omkarsathe.outvoice.auth;

import com.omkarsathe.outvoice.workspace.CreateWorkspace;
import com.omkarsathe.outvoice.workspace.user.CreateUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SignupRequest(
        @Valid
        @NotNull
        CreateUser user,

        @Valid
        @NotNull
        CreateWorkspace workspace
) {}
