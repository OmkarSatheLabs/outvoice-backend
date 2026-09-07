package com.omkarsathe.outvoice.sms.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sms_outbox")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SmsOutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    private Instant nextAttemptAt;

    private Instant sentAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    private String providerMessageId;

    private String providerBatchId;

    private Instant createdAt;

    private Instant updatedAt;

    public static SmsOutbox pending(
            String recipient,
            String message
    ) {
        SmsOutbox outbox = new SmsOutbox();

        outbox.recipient = recipient;
        outbox.message = message;
        outbox.status = SmsOutboxStatus.PENDING;
        outbox.attempts = 0;
        outbox.createdAt = Instant.now();
        outbox.updatedAt = Instant.now();

        return outbox;
    }
}
