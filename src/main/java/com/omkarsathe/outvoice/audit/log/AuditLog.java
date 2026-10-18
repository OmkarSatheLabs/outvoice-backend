package com.omkarsathe.outvoice.audit.log;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workspace_id")
    private UUID workspaceId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "actor_name", length = 200)
    private String actorName;

    @Column(name = "actor_email", length = 200)
    private String actorEmail;

    @Column(name = "change_description", nullable = false, columnDefinition = "TEXT")
    private String changeDescription;

    @Column(nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode changes;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;
}
