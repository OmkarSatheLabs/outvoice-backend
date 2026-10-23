package com.omkarsathe.outvoice.auth.mfa;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.auth.mfa.dto.*;
import com.omkarsathe.outvoice.mail.MailRequest;
import com.omkarsathe.outvoice.mail.MailService;
import com.omkarsathe.outvoice.mail.MailType;
import com.omkarsathe.outvoice.security.JwtService;
import com.omkarsathe.outvoice.sms.SmsMessage;
import com.omkarsathe.outvoice.sms.SmsService;
import com.omkarsathe.outvoice.workspace.user.MfaChannel;
import com.omkarsathe.outvoice.workspace.user.User;
import com.omkarsathe.outvoice.workspace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MfaService {

    private final UserMfaChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final SmsService smsService;
    private final JwtService jwtService;
    private final AuditService auditService;

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    @Transactional
    public AuthResponse initiateLoginChallenge(User user) {
        String sessionToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(OTP_EXPIRY_MINUTES));

        MfaChannel channel = user.getMfaChannel() != null ? user.getMfaChannel() : MfaChannel.EMAIL;

        if (channel == MfaChannel.AUTHENTICATOR_APP) {
            UserMfaChallenge challenge = UserMfaChallenge.builder()
                    .user(user)
                    .sessionToken(sessionToken)
                    .deliveryChannel(MfaChannel.AUTHENTICATOR_APP.name())
                    .destination("Authenticator App")
                    .expiresAt(expiresAt)
                    .build();
            challengeRepository.save(challenge);

            return AuthResponse.ofMfa(sessionToken, MfaChannel.AUTHENTICATOR_APP.name(), "Authenticator App");
        }

        // Email or SMS
        String deliveryChannel = (channel == MfaChannel.SMS && user.getMobile() != null)
                ? MfaChannel.SMS.name()
                : MfaChannel.EMAIL.name();

        String code = generateRandomCode();
        String otpHash = hashOtp(code, sessionToken);
        String destination = deliveryChannel.equals(MfaChannel.SMS.name())
                ? maskPhone(user.getPhoneCode() != null ? user.getPhoneCode().getCode() : "", user.getMobile())
                : maskEmail(user.getEmail());

        UserMfaChallenge challenge = UserMfaChallenge.builder()
                .user(user)
                .sessionToken(sessionToken)
                .otpHash(otpHash)
                .deliveryChannel(deliveryChannel)
                .destination(destination)
                .expiresAt(expiresAt)
                .build();
        challengeRepository.save(challenge);

        dispatchOtp(user, deliveryChannel, code);

        return AuthResponse.ofMfa(sessionToken, deliveryChannel, destination);
    }

    @Transactional
    public AuthResponse verifyLoginChallenge(MfaVerifyRequest request) {
        UserMfaChallenge challenge = challengeRepository
                .findBySessionTokenAndVerifiedAtIsNull(request.mfaToken())
                .orElseThrow(() -> new BadCredentialsException("Verification session has expired or is invalid. Please log in again."));

        if (challenge.isExpired()) {
            throw new BadCredentialsException("Verification code has expired. Please log in again.");
        }

        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadCredentialsException("Too many failed attempts. Please log in again.");
        }

        boolean verified;
        if (MfaChannel.AUTHENTICATOR_APP.name().equals(challenge.getDeliveryChannel())) {
            String secret = challenge.getUser().getTotpSecret();
            verified = secret != null && TotpUtil.verifyCode(secret, request.code());
        } else {
            String expectedHash = hashOtp(request.code(), challenge.getSessionToken());
            verified = expectedHash.equals(challenge.getOtpHash());
        }

        if (!verified) {
            challenge.incrementAttempts();
            challengeRepository.save(challenge);
            throw new BadCredentialsException("Invalid verification code. Please try again.");
        }

        challenge.setVerifiedAt(Instant.now());
        challengeRepository.save(challenge);

        String jwt = jwtService.generateToken(challenge.getUser().getUsername());

        auditService.log(
                null,
                AuditAction.STATUS_CHANGE,
                AuditResourceType.USER,
                challenge.getUser().getId(),
                "User successfully verified MFA and logged in (" + challenge.getDeliveryChannel() + ")",
                null
        );

        return AuthResponse.ofToken(jwt);
    }

    @Transactional
    public Map<String, String> resendLoginChallenge(MfaResendRequest request) {
        UserMfaChallenge challenge = challengeRepository
                .findBySessionTokenAndVerifiedAtIsNull(request.mfaToken())
                .orElseThrow(() -> new BadCredentialsException("Verification session has expired or is invalid."));

        if (MfaChannel.AUTHENTICATOR_APP.name().equals(challenge.getDeliveryChannel())) {
            throw new IllegalArgumentException("Authenticator app codes update automatically in your app every 30 seconds.");
        }

        if (challenge.isExpired() || challenge.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadCredentialsException("Session is no longer valid. Please start login again.");
        }

        String newCode = generateRandomCode();
        challenge.setOtpHash(hashOtp(newCode, challenge.getSessionToken()));
        challenge.setExpiresAt(Instant.now().plus(Duration.ofMinutes(OTP_EXPIRY_MINUTES)));
        challengeRepository.save(challenge);

        dispatchOtp(challenge.getUser(), challenge.getDeliveryChannel(), newCode);

        return Map.of("message", "A new verification code has been sent.");
    }

    @Transactional
    public MfaSetupResponse fallbackToEmail(MfaResendRequest request) {
        UserMfaChallenge challenge = challengeRepository
                .findBySessionTokenAndVerifiedAtIsNull(request.mfaToken())
                .orElseThrow(() -> new BadCredentialsException("Verification session has expired or is invalid."));

        User user = challenge.getUser();
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("No registered email address on file for fallback verification.");
        }

        String code = generateRandomCode();
        String maskedEmail = maskEmail(user.getEmail());

        challenge.setDeliveryChannel(MfaChannel.EMAIL.name());
        challenge.setDestination(maskedEmail);
        challenge.setOtpHash(hashOtp(code, challenge.getSessionToken()));
        challenge.setExpiresAt(Instant.now().plus(Duration.ofMinutes(OTP_EXPIRY_MINUTES)));
        challengeRepository.save(challenge);

        mailService.queue(new MailRequest(user.getEmail(), MailType.OTP, Map.of("otp", code)));

        return new MfaSetupResponse(challenge.getSessionToken(), MfaChannel.EMAIL.name(), maskedEmail);
    }

    // --- SETUP FLOWS ---

    public TotpSetupResponse initiateTotpSetup(User user) {
        String secret = TotpUtil.generateSecret();
        String accountName = user.getEmail() != null && !user.getEmail().isBlank()
                ? user.getEmail()
                : (user.getMobile() != null ? user.getMobile() : user.getFullName());

        String uri = TotpUtil.getOtpAuthUri(secret, accountName, "OutVoice");
        return new TotpSetupResponse(secret, uri);
    }

    @Transactional
    public void confirmTotpSetup(User user, TotpConfirmRequest request) {
        if (!TotpUtil.verifyCode(request.secret(), request.code())) {
            throw new BadCredentialsException("Invalid verification code. Please check your authenticator app time.");
        }

        user.setTotpSecret(request.secret());
        user.setMfaChannel(MfaChannel.AUTHENTICATOR_APP);
        user.setMfaEnabled(true);
        userRepository.save(user);

        auditService.log(
                null,
                AuditAction.CONFIG_UPDATE,
                AuditResourceType.USER,
                user.getId(),
                "User enabled Multi-Factor Authentication with Authenticator App",
                null
        );
    }

    @Transactional
    public MfaSetupResponse initiateEmailSetup(User user) {
        String sessionToken = UUID.randomUUID().toString();
        String code = generateRandomCode();
        String otpHash = hashOtp(code, sessionToken);

        String channel = (user.getEmail() != null && !user.getEmail().isBlank())
                ? MfaChannel.EMAIL.name()
                : MfaChannel.SMS.name();

        String destination = channel.equals(MfaChannel.EMAIL.name())
                ? maskEmail(user.getEmail())
                : maskPhone(user.getPhoneCode() != null ? user.getPhoneCode().getCode() : "", user.getMobile());

        UserMfaChallenge challenge = UserMfaChallenge.builder()
                .user(user)
                .sessionToken(sessionToken)
                .otpHash(otpHash)
                .deliveryChannel(channel)
                .destination(destination)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(OTP_EXPIRY_MINUTES)))
                .build();
        challengeRepository.save(challenge);

        dispatchOtp(user, channel, code);

        return new MfaSetupResponse(sessionToken, channel, destination);
    }

    @Transactional
    public void confirmEmailSetup(User user, MfaConfirmRequest request) {
        UserMfaChallenge challenge = challengeRepository
                .findBySessionTokenAndVerifiedAtIsNull(request.sessionToken())
                .orElseThrow(() -> new BadCredentialsException("Activation session has expired. Please try again."));

        if (!challenge.getUser().getId().equals(user.getId()) || challenge.isExpired()) {
            throw new BadCredentialsException("Activation session is invalid or expired.");
        }

        String expectedHash = hashOtp(request.code(), challenge.getSessionToken());
        if (!expectedHash.equals(challenge.getOtpHash())) {
            challenge.incrementAttempts();
            challengeRepository.save(challenge);
            throw new BadCredentialsException("Invalid verification code.");
        }

        challenge.setVerifiedAt(Instant.now());
        challengeRepository.save(challenge);

        user.setMfaChannel(MfaChannel.valueOf(challenge.getDeliveryChannel()));
        user.setMfaEnabled(true);
        userRepository.save(user);

        auditService.log(
                null,
                AuditAction.CONFIG_UPDATE,
                AuditResourceType.USER,
                user.getId(),
                "User enabled Multi-Factor Authentication with " + challenge.getDeliveryChannel(),
                null
        );
    }

    @Transactional
    public void disableMfa(User user) {
        user.setMfaEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);

        challengeRepository.deleteAllByUserId(user.getId());

        auditService.log(
                null,
                AuditAction.CONFIG_UPDATE,
                AuditResourceType.USER,
                user.getId(),
                "User disabled Multi-Factor Authentication",
                null
        );
    }

    // --- HELPERS ---

    private void dispatchOtp(User user, String channel, String code) {
        if (MfaChannel.EMAIL.name().equals(channel) && user.getEmail() != null) {
            mailService.queue(new MailRequest(user.getEmail(), MailType.OTP, Map.of("otp", code)));
        } else if (MfaChannel.SMS.name().equals(channel) && user.getMobile() != null) {
            String fullPhone = (user.getPhoneCode() != null ? user.getPhoneCode().getCode() : "") + user.getMobile();
            smsService.queue(new SmsMessage(fullPhone, "Your OutVoice verification code is: " + code));
        }
    }

    private String generateRandomCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private String hashOtp(String otp, String sessionToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((sessionToken + ":" + otp).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to calculate SHA-256 hash", e);
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        String domain = email.substring(at);
        if (name.length() <= 2) {
            return name.charAt(0) + "***" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }

    private String maskPhone(String phoneCode, String mobile) {
        if (mobile == null || mobile.length() < 4) return (phoneCode != null ? phoneCode : "") + mobile;
        String prefix = phoneCode != null && !phoneCode.isBlank() ? phoneCode + " " : "";
        String last4 = mobile.substring(mobile.length() - 4);
        return prefix + "*****" + last4;
    }
}
