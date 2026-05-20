package com.omkarsathe.outvoice.audit;

import com.omkarsathe.outvoice.organization.Organization;
import com.omkarsathe.outvoice.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/** Immutable audit record written by AuditLogService for every significant role/permission event. */
@Entity
@Table(name = "audit_logs")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Null for platform-level events. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization org;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User target;

    /** JSON snapshot of the state before the change. */
    @Column(columnDefinition = "TEXT")
    private String oldValue;

    /** JSON snapshot of the state after the change. */
    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(length = 50)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
