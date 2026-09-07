//package com.omkarsathe.outvoice.user.workspace.invite;
//
//import com.omkarsathe.outvoice.phone.PhoneCode;
////import com.omkarsathe.outvoice.user.UserEntity;
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
//@Table(name = "invites")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//class InviteEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "workspace_id", nullable = false)
//    private Workspace workspaceId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private UserEntity userId;
//
//    @Column
//    private String email;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "phone_code_id")
//    private PhoneCode phoneCodeId;
//
//    @Column
//    private String mobile;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "invited_by", nullable = false)
//    private UserEntity invitedBy;
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by")
//    private UserEntity createdBy;
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
//
//    @Column
//    private LocalDateTime expiredAt;
//}
