package com.omkarsathe.outvoice.auth.mfa;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.auth.mfa.dto.MfaResendRequest;
import com.omkarsathe.outvoice.auth.mfa.dto.MfaVerifyRequest;
import com.omkarsathe.outvoice.auth.mfa.dto.TotpConfirmRequest;
import com.omkarsathe.outvoice.mail.MailService;
import com.omkarsathe.outvoice.security.JwtService;
import com.omkarsathe.outvoice.sms.SmsService;
import com.omkarsathe.outvoice.workspace.user.MfaChannel;
import com.omkarsathe.outvoice.workspace.user.User;
import com.omkarsathe.outvoice.workspace.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private UserMfaChallengeRepository challengeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailService mailService;

    @Mock
    private SmsService smsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private MfaService mfaService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Test User")
                .email("test@outvoice.app")
                .passwordHash("hashedPass")
                .mfaEnabled(true)
                .mfaChannel(MfaChannel.AUTHENTICATOR_APP)
                .totpSecret(TotpUtil.generateSecret())
                .build();
    }

    @Test
    @DisplayName("Should initiate login challenge for Authenticator App")
    void shouldInitiateLoginChallengeForAuthenticatorApp() {
        AuthResponse res = mfaService.initiateLoginChallenge(testUser);

        assertTrue(res.isMfaRequired());
        assertEquals("AUTHENTICATOR_APP", res.getDeliveryChannel());
        assertNotNull(res.getMfaToken());
        verify(challengeRepository).save(any(UserMfaChallenge.class));
        verifyNoInteractions(mailService);
    }

    @Test
    @DisplayName("Should initiate login challenge for Email and queue email OTP")
    void shouldInitiateLoginChallengeForEmail() {
        testUser.setMfaChannel(MfaChannel.EMAIL);
        AuthResponse res = mfaService.initiateLoginChallenge(testUser);

        assertTrue(res.isMfaRequired());
        assertEquals("EMAIL", res.getDeliveryChannel());
        assertTrue(res.getMaskedDestination().contains("***"));
        verify(mailService).queue(any());
        verify(challengeRepository).save(any(UserMfaChallenge.class));
    }

    @Test
    @DisplayName("Should verify valid TOTP login challenge and return JWT")
    void shouldVerifyValidTotpLoginChallenge() {
        String token = "session-123";
        String validCode = TotpUtil.generateCurrentCode(testUser.getTotpSecret());

        UserMfaChallenge challenge = UserMfaChallenge.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .sessionToken(token)
                .deliveryChannel("AUTHENTICATOR_APP")
                .expiresAt(Instant.now().plusSeconds(300))
                .attempts(0)
                .build();

        when(challengeRepository.findBySessionTokenAndVerifiedAtIsNull(token)).thenReturn(Optional.of(challenge));
        when(jwtService.generateToken(testUser.getUsername())).thenReturn("mock-jwt-token");

        AuthResponse auth = mfaService.verifyLoginChallenge(new MfaVerifyRequest(token, validCode));

        assertFalse(auth.isMfaRequired());
        assertEquals("mock-jwt-token", auth.getToken());
        assertNotNull(challenge.getVerifiedAt());
        verify(challengeRepository).save(challenge);
    }

    @Test
    @DisplayName("Should reject invalid TOTP code and increment attempts")
    void shouldRejectInvalidTotpCode() {
        String token = "session-123";

        UserMfaChallenge challenge = UserMfaChallenge.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .sessionToken(token)
                .deliveryChannel("AUTHENTICATOR_APP")
                .expiresAt(Instant.now().plusSeconds(300))
                .attempts(0)
                .build();

        when(challengeRepository.findBySessionTokenAndVerifiedAtIsNull(token)).thenReturn(Optional.of(challenge));

        assertThrows(BadCredentialsException.class, () ->
                mfaService.verifyLoginChallenge(new MfaVerifyRequest(token, "999999")));

        assertEquals(1, challenge.getAttempts());
        verify(challengeRepository).save(challenge);
    }

    @Test
    @DisplayName("Should allow email fallback from Authenticator App session")
    void shouldAllowFallbackToEmail() {
        String token = "session-123";

        UserMfaChallenge challenge = UserMfaChallenge.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .sessionToken(token)
                .deliveryChannel("AUTHENTICATOR_APP")
                .expiresAt(Instant.now().plusSeconds(300))
                .attempts(0)
                .build();

        when(challengeRepository.findBySessionTokenAndVerifiedAtIsNull(token)).thenReturn(Optional.of(challenge));

        var fallbackRes = mfaService.fallbackToEmail(new MfaResendRequest(token));

        assertEquals("EMAIL", fallbackRes.deliveryChannel());
        assertEquals("EMAIL", challenge.getDeliveryChannel());
        verify(mailService).queue(any());
        verify(challengeRepository).save(challenge);
    }

    @Test
    @DisplayName("Should confirm TOTP setup and enable MFA on user")
    void shouldConfirmTotpSetup() {
        String secret = TotpUtil.generateSecret();
        String validCode = TotpUtil.generateCurrentCode(secret);

        testUser.setMfaEnabled(false);
        testUser.setTotpSecret(null);

        mfaService.confirmTotpSetup(testUser, new TotpConfirmRequest(secret, validCode));

        assertTrue(testUser.isMfaEnabled());
        assertEquals(MfaChannel.AUTHENTICATOR_APP, testUser.getMfaChannel());
        assertEquals(secret, testUser.getTotpSecret());
        verify(userRepository).save(testUser);
        verify(auditService).log(isNull(), eq(AuditAction.CONFIG_UPDATE), eq(AuditResourceType.USER), any(), any(), isNull());
    }

    @Test
    @DisplayName("Should disable MFA on user and purge challenges")
    void shouldDisableMfa() {
        mfaService.disableMfa(testUser);

        assertFalse(testUser.isMfaEnabled());
        assertNull(testUser.getTotpSecret());
        verify(userRepository).save(testUser);
        verify(challengeRepository).deleteAllByUserId(testUser.getId());
        verify(auditService).log(isNull(), eq(AuditAction.CONFIG_UPDATE), eq(AuditResourceType.USER), any(), any(), isNull());
    }
}
