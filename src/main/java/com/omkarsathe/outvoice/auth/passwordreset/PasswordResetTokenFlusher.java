package com.omkarsathe.outvoice.auth.passwordreset;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenFlusher {

    private static final Logger logger =
            Logger.getLogger(PasswordResetTokenFlusher.class.getName());

    private static final long RETENTION_DAYS = 7;

    private final PasswordResetTokenRepository repository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void flush() {

        Instant cutoff = Instant.now()
                .minus(RETENTION_DAYS, ChronoUnit.DAYS);

        int deleted = repository.deleteOlderThan(cutoff);

        if (deleted > 0) {
            logger.info(
                    "Deleted " + deleted +
                            " password reset token(s) older than " +
                            RETENTION_DAYS + " days"
            );
        }
    }
}
