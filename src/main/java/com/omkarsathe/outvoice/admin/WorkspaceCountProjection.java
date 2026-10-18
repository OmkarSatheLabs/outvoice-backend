package com.omkarsathe.outvoice.admin;

import java.util.UUID;

public interface WorkspaceCountProjection {
    UUID getWorkspaceId();
    Long getTotal();
}
