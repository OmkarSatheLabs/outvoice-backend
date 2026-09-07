package com.omkarsathe.outvoice.mail.outbox;

import com.omkarsathe.outvoice.mail.MailType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "mail_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> variables;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> attachmentPaths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MailStatus status = MailStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    private Instant nextAttemptAt;

    private Instant sentAt;

    @Column(length = 2000)
    private String lastError;
}
