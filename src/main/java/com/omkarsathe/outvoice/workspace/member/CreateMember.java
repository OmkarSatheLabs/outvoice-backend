package com.omkarsathe.outvoice.workspace.member;

import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.user.User;

public record CreateMember(
    Workspace workspace,
    User user
) {}
