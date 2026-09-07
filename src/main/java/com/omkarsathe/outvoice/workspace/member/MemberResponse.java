package com.omkarsathe.outvoice.workspace.member;

import com.omkarsathe.outvoice.workspace.WorkspaceResponse;
import com.omkarsathe.outvoice.workspace.user.UserResponse;

import java.util.UUID;

public record MemberResponse(
        UUID id,
        UserResponse user
) {}
