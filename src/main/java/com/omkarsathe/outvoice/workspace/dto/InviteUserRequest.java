package com.omkarsathe.outvoice.workspace.dto;

//import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserRequest {
    private String email;
    private String mobile;
    private UUID phoneCodeId;

//    @NotNull(message = "Role is required")
//    private WorkspaceRole role;

    private Set<UUID> teamIds;
    private Set<UUID> customRoleIds;
}
