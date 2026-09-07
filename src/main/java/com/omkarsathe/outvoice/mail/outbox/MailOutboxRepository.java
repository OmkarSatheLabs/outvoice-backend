package com.omkarsathe.outvoice.mail.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MailOutboxRepository extends JpaRepository<MailOutbox, UUID> {

    List<MailOutbox> findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
            MailStatus status,
            Instant now
    );
}
