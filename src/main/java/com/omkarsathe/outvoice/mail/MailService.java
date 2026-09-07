package com.omkarsathe.outvoice.mail;

import com.omkarsathe.outvoice.common.exception.MailException;
import com.omkarsathe.outvoice.mail.outbox.MailOutbox;
import com.omkarsathe.outvoice.mail.outbox.MailOutboxRepository;
import com.omkarsathe.outvoice.mail.outbox.MailStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class MailService {

    private final Logger logger = Logger.getLogger(MailService.class.getName());
    private final JavaMailSender mailSender;
    private final MailOutboxRepository mailOutboxRepository;

    public void send(MailOutbox request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(request.getRecipient());
            helper.setSubject(getSubject(request.getType()));

            String body = buildBody(new MailRequest(request.getRecipient(), request.getType(), request.getVariables()));

            helper.setText(body, true);

            for (String path : request.getAttachmentPaths()) {
                File file = new File(path);
                if (file.exists()) {
                    helper.addAttachment(file.getName(), file);
                } else {
                    logger.severe("Attachment missing at send time: " + path);
                }
            }

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new MailException(
                    "Failed to send " + request.getType() + " email to " + request.getRecipient(),
                    e
            );
        }
    }

    private String getSubject(MailType type) {
        return switch (type) {
            case WELCOME -> "Welcome to OutVoice";
            case OTP -> "Your OutVoice verification code";
            case INVOICE -> "Your OutVoice invoice";
            case WORKSPACE_INVITATION -> "You've been invited to an OutVoice workspace";
            case PASSWORD_RESET -> "Reset your OutVoice password";
        };
    }

    private String buildBody(MailRequest request) {
        return switch (request.type()) {
            case WELCOME -> buildWelcomeEmail(request);
            case OTP -> buildOtpEmail(request);
            case INVOICE -> buildInvoiceEmail(request);
            case WORKSPACE_INVITATION -> buildWorkspaceInvitationEmail(request);
            case PASSWORD_RESET -> buildPasswordResetEmail(request);
        };
    }

    private String buildWelcomeEmail(MailRequest request) {
        String name = String.valueOf(request.variables().get("name"));

        return """
                <html>
                    <body>
                        <h1>Welcome to OutVoice, %s!</h1>
                        <p>Your account has been successfully created.</p>
                        <p>We're glad to have you with us.</p>
                    </body>
                </html>
                """.formatted(name);
    }

    private String buildOtpEmail(MailRequest request) {
        String otp = String.valueOf(request.variables().get("otp"));

        return """
                <html>
                    <body>
                        <h1>Your verification code</h1>
                        <p>Your OutVoice verification code is:</p>
                        <h2>%s</h2>
                        <p>This code will expire shortly.</p>
                    </body>
                </html>
                """.formatted(otp);
    }

    private String buildInvoiceEmail(MailRequest request) {
        String invoiceNumber =
                String.valueOf(request.variables().get("invoiceNumber"));

        return """
                <html>
                    <body>
                        <h1>Invoice %s</h1>
                        <p>Your invoice is ready.</p>
                    </body>
                </html>
                """.formatted(invoiceNumber);
    }

    private String buildWorkspaceInvitationEmail(MailRequest request) {
        String workspaceName =
                String.valueOf(request.variables().get("workspaceName"));

        String invitationUrl =
                String.valueOf(request.variables().get("invitationUrl"));

        return """
                <html>
                    <body>
                        <h1>You're invited!</h1>
                        <p>
                            You've been invited to join
                            <strong>%s</strong> on OutVoice.
                        </p>
                        <p>
                            <a href="%s">Accept invitation</a>
                        </p>
                    </body>
                </html>
                """.formatted(workspaceName, invitationUrl);
    }

    private String buildPasswordResetEmail(MailRequest request) {
        String resetUrl =
                String.valueOf(request.variables().get("resetUrl"));

        return """
                <html>
                    <body>
                        <h1>Reset your password</h1>
                        <p>
                            Click the link below to reset your OutVoice password.
                        </p>
                        <p>
                            <a href="%s">Reset password</a>
                        </p>
                    </body>
                </html>
                """.formatted(resetUrl);
    }

    @Transactional
    public void queue(MailRequest request) {

        logger.info("Queueing " + request.type().name().toLowerCase() + " email");

        MailOutbox mail = MailOutbox.builder()
                .recipient(request.to())
                .type(request.type())
                .variables(request.variables())
                .status(MailStatus.PENDING)
                .attempts(0)
                .nextAttemptAt(Instant.now())
                .attachmentPaths(request.attachmentPaths())
                .build();

        MailOutbox saved = mailOutboxRepository.save(mail);

        logger.info("Queued " + request.type().name().toLowerCase() + " email with id: " + saved.getId());
    }
}
