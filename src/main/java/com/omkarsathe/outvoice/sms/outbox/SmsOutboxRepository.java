package com.omkarsathe.outvoice.sms.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SmsOutboxRepository extends JpaRepository<SmsOutbox, UUID> {

    @Query("""
        SELECT s
        FROM SmsOutbox s
        WHERE s.status = :status
          AND (s.nextAttemptAt IS NULL OR s.nextAttemptAt <= :now)
        ORDER BY s.createdAt ASC
        """)
    List<SmsOutbox> findPendingMessages(
            @Param("status") SmsOutboxStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );
}
