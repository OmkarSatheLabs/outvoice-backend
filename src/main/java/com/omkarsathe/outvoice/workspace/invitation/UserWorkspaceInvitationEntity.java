package com.omkarsathe.outvoice.workspace.invitation;

import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.workspace.WorkspaceEntity;
import com.omkarsathe.outvoice.workspace.role.CustomRole;
import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import com.omkarsathe.outvoice.workspace.team.Team;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_workspace_invitations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkspaceInvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_code_id")
    private PhoneCode phoneCode;

    private String mobile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private UserEntity invitedBy;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, ACCEPTED, DECLINED, EXPIRED

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @ManyToMany
    @JoinTable(
            name = "workspace_invitation_teams",
            joinColumns = @JoinColumn(name = "invitation_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<Team> teams;

    @ManyToMany
    @JoinTable(
            name = "workspace_invitation_roles",
            joinColumns = @JoinColumn(name = "invitation_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<CustomRole> customRoles;
}
