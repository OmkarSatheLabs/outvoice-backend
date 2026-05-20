package com.omkarsathe.outvoice.organization;

import com.omkarsathe.outvoice.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Tracks outgoing org invitations for both MEMBER and SUPER_USER roles. */
@Entity
@Table(name = "org_invitations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    @Column
    private String inviteeEmail;

    @Column
    private String inviteeMobile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgRole intendedRole;

    /** Permissions to assign on acceptance; only relevant when intendedRole = MEMBER. */
    @Convert(converter = PermissionListConverter.class)
    @Column(name = "intended_permissions", columnDefinition = "TEXT")
    private List<Permission> intendedPermissions;

    /** Meaningful only for SUPER_USER invites — must be APPROVED before the invite is sent. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvitationApprovalStatus approvalStatus = InvitationApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    /** Secure random token used in the invite link. */
    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
