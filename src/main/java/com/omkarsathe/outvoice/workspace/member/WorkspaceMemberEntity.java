//package com.omkarsathe.outvoice.workspace.member;
//
//import com.omkarsathe.outvoice.user.UserEntity;
//import com.omkarsathe.outvoice.user.plan.PlanEntity;
//import com.omkarsathe.outvoice.user.workspace.role.RoleEntity;
//import com.omkarsathe.outvoice.workspace.Workspace;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "workspace_members")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class WorkspaceMemberEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "workspace_id")
//    private Workspace workspace;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "role_id")
//    private RoleEntity role;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "plan_id")
//    private PlanEntity plan;
//
//    @Column()
//    private LocalDateTime invitedAt;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "invited_by_id")
//    private UserEntity invitedBy;
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column
//    private LocalDateTime updatedAt;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "updated_by")
//    private UserEntity updatedBy;
//
//    @Column
//    private LocalDateTime deletedAt;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "deleted_by")
//    private UserEntity deletedBy;
//}
