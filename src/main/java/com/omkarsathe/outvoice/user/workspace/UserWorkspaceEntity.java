//package com.omkarsathe.outvoice.user.workspace;
//
//import com.omkarsathe.outvoice.user.UserEntity;
//import com.omkarsathe.outvoice.workspace.Workspace;
//import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
////import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "user_workspaces")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class UserWorkspaceEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private UserEntity user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "workspace_id", nullable = false)
//    private Workspace workspace;
//
////    @Enumerated(EnumType.STRING)
////    @Column(nullable = false)
////    private WorkspaceRole role;
//
//    @Column(nullable = false)
//    @Builder.Default
//    private Boolean isDefaultWorkspace = false;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "invited_by")
//    private UserEntity invitedBy;
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime joinedAt;
//
//    @Enumerated(EnumType.STRING)
//    private MemberStatusEnum status;
//
////    @ManyToMany
////    @JoinTable(
////            name = "user_workspace_teams",
////            joinColumns = @JoinColumn(name = "user_workspace_id"),
////            inverseJoinColumns = @JoinColumn(name = "team_id")
////    )
////    private Set<Team> teams;
//
////    @ManyToMany
////    @JoinTable(
////            name = "user_workspace_roles",
////            joinColumns = @JoinColumn(name = "user_workspace_id"),
////            inverseJoinColumns = @JoinColumn(name = "role_id")
////    )
////    private Set<CustomRole> customRoles;
//}
