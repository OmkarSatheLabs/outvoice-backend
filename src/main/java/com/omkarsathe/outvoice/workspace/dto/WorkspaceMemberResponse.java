package com.omkarsathe.outvoice.workspace.dto;

//import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String mobile;
//    private WorkspaceRole role;
    private String status;
    private LocalDateTime joinedAt;
    private List<TeamResponse> teams;
    private List<CustomRoleResponse> customRoles;
}
