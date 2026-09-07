package com.omkarsathe.outvoice.workspace;

//import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;

import java.util.Set;
import java.util.UUID;

public record WorkspacePrincipal(UUID userId, UUID workspaceId, Set<Permission> permissions) {

    public boolean hasPermission(Permission p) {
        return permissions.contains(p);
    }
}
