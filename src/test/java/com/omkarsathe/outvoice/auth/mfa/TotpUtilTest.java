package com.omkarsathe.outvoice.auth.mfa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TotpUtilTest {

    @Test
    @DisplayName("Should generate a valid 32-character Base32 secret")
    void shouldGenerateValidBase32Secret() {
        String secret = TotpUtil.generateSecret();
        assertNotNull(secret);
        assertEquals(32, secret.length());
        assertTrue(secret.matches("^[A-Z2-7]+$"));
    }

    @Test
    @DisplayName("Should encode and decode Base32 bytes accurately")
    void shouldEncodeAndDecodeBase32() {
        String original = "OutVoiceSecretKey123";
        byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);

        String encoded = TotpUtil.encodeBase32(originalBytes);
        assertNotNull(encoded);

        byte[] decodedBytes = TotpUtil.decodeBase32(encoded);
        assertEquals(original, new String(decodedBytes, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Should build valid otpauth URI")
    void shouldBuildValidOtpAuthUri() {
        String secret = "JBSWY3DPEHPK3PXP";
        String uri = TotpUtil.getOtpAuthUri(secret, "alice@outvoice.app", "OutVoice");

        assertTrue(uri.startsWith("otpauth://totp/OutVoice:alice%40outvoice.app?secret=" + secret));
        assertTrue(uri.contains("issuer=OutVoice"));
        assertTrue(uri.contains("period=30"));
        assertTrue(uri.contains("digits=6"));
    }

    @Test
    @DisplayName("Should verify currently generated TOTP code")
    void shouldVerifyGeneratedCode() {
        String secret = TotpUtil.generateSecret();
        String currentCode = TotpUtil.generateCurrentCode(secret);

        assertNotNull(currentCode);
        assertEquals(6, currentCode.length());
        assertTrue(TotpUtil.verifyCode(secret, currentCode));
        assertFalse(TotpUtil.verifyCode(secret, "000000".equals(currentCode) ? "999999" : "000000"));
    }
}
