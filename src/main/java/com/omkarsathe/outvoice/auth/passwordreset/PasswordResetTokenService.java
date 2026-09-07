package com.omkarsathe.outvoice.auth.passwordreset;

import com.omkarsathe.outvoice.common.exception.BadRequestException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.mail.MailRequest;
import com.omkarsathe.outvoice.mail.MailService;
import com.omkarsathe.outvoice.mail.MailType;
import com.omkarsathe.outvoice.mail.outbox.MailOutbox;
import com.omkarsathe.outvoice.sms.SmsMessage;
import com.omkarsathe.outvoice.sms.SmsService;
import com.omkarsathe.outvoice.workspace.user.User;
import com.omkarsathe.outvoice.workspace.user.UserRepository;
import com.omkarsathe.outvoice.workspace.user.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${password-reset.expiry-ms}")
    private long expiryMs;

    private final Logger logger = Logger.getLogger(PasswordResetTokenService.class.getName());
    private final UserService userService;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final SmsService smsService;
    private final PasswordResetTokenRepository repository;

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {

        boolean hasEmail = request.email() != null && !request.email().isBlank();

        boolean hasPhone = request.phoneCode() != null
                && !request.phoneCode().isBlank()
                && request.mobile() != null
                && !request.mobile().isBlank();

        // Nothing usable supplied
        if (!hasEmail && !hasPhone) {
            return buildGenericResponse(null);
        }

        Optional<User> userOptional;

        if (hasEmail && hasPhone) {

            // If both are supplied, make sure they belong to the same account
            userOptional = userRepository.findByEmailAndPhoneCode_CodeAndMobile(
                    request.email(),
                    request.phoneCode(),
                    request.mobile()
            );

        } else if (hasEmail) {

            userOptional = userRepository.findByEmail(request.email());

        } else {

            userOptional = userRepository.findByPhoneCode_CodeAndMobile(
                    request.phoneCode(),
                    request.mobile()
            );
        }

        // Generic response prevents account enumeration
        if (userOptional.isEmpty()) {
            return buildGenericResponse(hasEmail ? request.email() : null);
        }

        User user = userOptional.get();

        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expiryMs);

        // Invalidate existing active tokens
        int invalidated = repository.invalidateActiveTokens(user.getId(), now);

        logger.info(
                "Invalidated " + invalidated + " active password reset token(s) for user ID: " + user.getId()
        );

        // Generate a new reset token
        String resetToken = generateResetToken();
        String tokenHash = hashToken(resetToken);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();

        repository.save(token);

        // Build reset link
        String resetLink = frontendUrl
                + "/reset-password?token="
                + URLEncoder.encode(resetToken, StandardCharsets.UTF_8);

        Map<String, Object> variables = Map.of(
                "resetUrl", resetLink
        );

        /*
         * Send through the channel requested by the user.
         *
         * Email request -> email
         * Phone request -> SMS
         *
         * If both were supplied, both channels are used.
         */
        if (hasEmail && user.getEmail() != null && !user.getEmail().isBlank()) {

            mailService.queue(new MailRequest(
                    user.getEmail(),
                    MailType.PASSWORD_RESET,
                    variables
            ));

            logger.info(
                    "Password reset email queued for user ID: " + user.getId()
            );
        }

        if (hasPhone
                && user.getPhoneCode() != null
                && user.getPhoneCode().getCode() != null
                && !user.getPhoneCode().getCode().isBlank()
                && user.getMobile() != null
                && !user.getMobile().isBlank()) {

            String phoneNumber =
                    user.getPhoneCode().getCode() + user.getMobile();

            String message =
                    "Hello " + user.getFullName()
                            + ",\nPlease follow this link to reset your password: "
                            + resetLink;

            smsService.queue(new SmsMessage(
                    phoneNumber,
                    message
            ));

            logger.info(
                    "Password reset SMS sent for user ID: " + user.getId()
            );
        }

        logger.info(
                "Password reset token created for user ID: " + user.getId() + ", expires at: " + expiresAt
        );

        /*
         * Don't return the user's email when the request was made
         * using mobile number.
         */
        return buildGenericResponse(
                hasEmail ? request.email() : null
        );
    }

    private ForgotPasswordResponse buildGenericResponse(String recipient) {
        return new ForgotPasswordResponse(
                "If an account exists with the provided details, a password reset link has been dispatched.",
                recipient
        );
    }

    @Transactional
    public ForgotPasswordResponse resetPassword(
            ResetPasswordRequest request
    ) {

        logger.info("Password reset attempt received");

        String tokenHash = hashToken(request.token());

        PasswordResetToken token = repository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    logger.warning("Password reset failed: token not found");
                    return new BadRequestException(
                            "Invalid or expired password reset token."
                    );
                });

        if (token.getUsedAt() != null) {
            logger.warning(
                    "Password reset failed: token already used for user: "
                            + token.getUser().getId()
            );

            throw new BadRequestException(
                    "Invalid or expired password reset token."
            );
        }

        Instant now = Instant.now();

        if (now.isAfter(token.getExpiresAt())) {

            logger.warning(
                    "Password reset failed: token expired for user: "
                            + token.getUser().getId()
            );

            repository.delete(token);

            throw new BadRequestException(
                    "Invalid or expired password reset token."
            );
        }

        User user = token.getUser();

        userService.updatePassword(
                user,
                request.newPassword()
        );

        token.setUsedAt(now);
        repository.save(token);

        logger.info(
                "Password successfully reset for user: " +
                        user.getId()
        );

        return new ForgotPasswordResponse(
                "Password has been reset successfully.",
                user.getEmail()
        );
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
