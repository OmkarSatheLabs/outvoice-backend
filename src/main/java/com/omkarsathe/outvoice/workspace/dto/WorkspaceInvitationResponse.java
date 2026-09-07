package com.omkarsathe.outvoice.workspace.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInvitationResponse {
    private UUID id;
    private UUID workspaceId;
    private String workspaceName;
    private String workspaceSlug;
    private String email;
    private String mobile;
    private String invitedBy;
    private String token;
    private String status;
    private String joiningLink;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Set<UUID> teamIds;
    private Set<UUID> customRoleIds;
}
