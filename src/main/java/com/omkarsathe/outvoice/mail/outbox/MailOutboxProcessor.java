package com.omkarsathe.outvoice.mail.outbox;

import com.omkarsathe.outvoice.mail.MailRequest;
import com.omkarsathe.outvoice.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class MailOutboxProcessor {

    private final Logger logger = Logger.getLogger(MailOutboxProcessor.class.getName());
    private final MailOutboxRepository mailOutboxRepository;
    private final MailService mailService;

    @Scheduled(fixedDelay = 5000)
    public void process() {
        logger.fine("Processing Mail Outbox");

        List<MailOutbox> mails =
                mailOutboxRepository
                        .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                                MailStatus.PENDING,
                                Instant.now()
                        );

        logger.fine("Found " + mails.size() + " Mail Outbox");

        for (MailOutbox mail : mails) {
            processMail(mail);
        }
    }

    private void processMail(MailOutbox mail) {
        logger.info("Processing mail for " + mail.getRecipient());

        try {
            mail.setStatus(MailStatus.PROCESSING);
            mailOutboxRepository.save(mail);

            mailService.send(mail);

            mail.setStatus(MailStatus.SENT);
            mail.setSentAt(Instant.now());

            logger.info("Mail sent to " + mail.getRecipient());
        } catch (Exception e) {
            handleFailure(mail, e);
        }

        mailOutboxRepository.save(mail);
    }

    private void handleFailure(MailOutbox mail, Exception exception) {

        logger.warning("Mail Failed with exception: " + exception);

        int attempts = mail.getAttempts() + 1;

        mail.setAttempts(attempts);
        mail.setLastError(exception.getMessage());

        if (attempts >= 5) {
            mail.setStatus(MailStatus.FAILED);

            logger.severe("Mail Failed with exception: " + exception);

            return;
        }

        long delaySeconds = (long) Math.pow(2, attempts) * 30;

        mail.setStatus(MailStatus.PENDING);
        mail.setNextAttemptAt(
                Instant.now().plusSeconds(delaySeconds)
        );
    }
}
