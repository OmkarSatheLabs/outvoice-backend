package com.omkarsathe.outvoice.sms.outbox;

import com.omkarsathe.outvoice.sms.SmsMessage;
import com.omkarsathe.outvoice.sms.SmsProvider;
import com.omkarsathe.outvoice.sms.SmsSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class SmsOutboxProcessor {

    private final Logger logger = Logger.getLogger(SmsOutboxProcessor.class.getName());
    private final SmsOutboxRepository repository;
    private final SmsProvider smsProvider;

    private static final int MAX_ATTEMPTS = 5;
    private static final long RETRY_BASE_DELAY_SECONDS = 30;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void process() {

        List<SmsOutbox> messages =
                repository.findPendingMessages(
                        SmsOutboxStatus.PENDING,
                        Instant.now(),
                        PageRequest.of(0, 20)
                );

        for (SmsOutbox sms : messages) {
            processSms(sms);
        }
    }

    private void processSms(SmsOutbox sms) {

        logger.info("Processing sms for " + sms.getRecipient());

        try {
            sms.setStatus(SmsOutboxStatus.PROCESSING);

            SmsSendResult result = smsProvider.send(
                    new SmsMessage(
                            sms.getRecipient(),
                            sms.getMessage()
                    )
            );

            sms.setStatus(SmsOutboxStatus.SENT);
            sms.setSentAt(Instant.now());
            sms.setNextAttemptAt(null);
            sms.setLastError(null);
            sms.setProviderMessageId(result.providerMessageId());
            sms.setProviderBatchId(result.providerBatchId());

            logger.info("Sms sent to " + sms.getRecipient());

        } catch (Exception e) {
            handleFailure(sms, e);
        }

        repository.save(sms);
    }

    private void handleFailure(SmsOutbox sms, Exception exception) {

        logger.warning("Sms Failed with exception: " + exception);

        int attempts = sms.getAttempts() + 1;

        sms.setAttempts(attempts);
        sms.setLastError(exception.getMessage());

        if (attempts >= MAX_ATTEMPTS) {
            sms.setStatus(SmsOutboxStatus.FAILED);

            logger.severe("Sms Failed with exception: " + exception);

            return;
        }

        long delaySeconds = (long) Math.pow(2, attempts) * RETRY_BASE_DELAY_SECONDS;

        sms.setStatus(SmsOutboxStatus.PENDING);
        sms.setNextAttemptAt(
                Instant.now().plusSeconds(delaySeconds)
        );
    }
}
