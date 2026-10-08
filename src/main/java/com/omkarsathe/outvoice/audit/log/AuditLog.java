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

    @Column(nullable = false, length = 100)
    private String resourceType;

    @Column(nullable = false)
    private UUID changedBy;

    @Column(nullable = false)
    private UUID resourceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String changeDescription;

    @Column(nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode changes;

    @Column(nullable = false)
    private Instant changedAt;
}
