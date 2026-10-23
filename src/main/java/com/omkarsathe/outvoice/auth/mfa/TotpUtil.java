package com.omkarsathe.outvoice.auth.mfa;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * Utility for RFC 6238 Time-Based One-Time Password (TOTP) and RFC 4648 Base32 encoding/decoding.
 */
public final class TotpUtil {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_BYTES_COUNT = 20; // 160 bits (recommended for SHA1)
    private static final int TIME_STEP_SECONDS = 30;
    private static final int DIGITS = 6;
    private static final int MOD_DIVISOR = 1_000_000;
    private static final String HMAC_ALGO = "HmacSHA1";

    private TotpUtil() {}

    /**
     * Generates a cryptographically random Base32 secret key.
     */
    public static String generateSecret() {
        byte[] buffer = new byte[SECRET_BYTES_COUNT];
        new SecureRandom().nextBytes(buffer);
        return encodeBase32(buffer);
    }

    /**
     * Builds standard otpauth URI for QR codes.
     */
    public static String getOtpAuthUri(String secret, String accountName, String issuer) {
        String safeAccount = URLEncoder.encode(accountName, StandardCharsets.UTF_8).replace("+", "%20");
        String safeIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8).replace("+", "%20");

        return "otpauth://totp/" + safeIssuer + ":" + safeAccount
                + "?secret=" + secret
                + "&issuer=" + safeIssuer
                + "&algorithm=SHA1"
                + "&digits=" + DIGITS
                + "&period=" + TIME_STEP_SECONDS;
    }

    /**
     * Generates the 6-digit TOTP for the current timestamp.
     */
    public static String generateCurrentCode(String base32Secret) {
        long currentStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        return generateCode(base32Secret, currentStep);
    }

    /**
     * Generates the 6-digit TOTP for a specific time step.
     */
    public static String generateCode(String base32Secret, long timeStep) {
        byte[] key = decodeBase32(base32Secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();

        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(key, HMAC_ALGO));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % MOD_DIVISOR;
            return String.format("%0" + DIGITS + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to calculate TOTP HMAC", e);
        }
    }

    /**
     * Verifies code allowing a ±1 step window (30 seconds before and after).
     */
    public static boolean verifyCode(String base32Secret, String code) {
        if (base32Secret == null || code == null || !code.matches("\\d{" + DIGITS + "}")) {
            return false;
        }

        long currentStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;

        for (int i = -1; i <= 1; i++) {
            String candidate = generateCode(base32Secret, currentStep + i);
            if (candidate.equals(code)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Encodes byte array to Base32 string (without padding).
     */
    public static String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                result.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }

        if (bitsLeft > 0) {
            buffer <<= (5 - bitsLeft);
            result.append(BASE32_CHARS.charAt(buffer & 0x1F));
        }

        return result.toString();
    }

    /**
     * Decodes Base32 string into byte array.
     */
    public static byte[] decodeBase32(String base32) {
        String cleaned = base32.trim().toUpperCase().replace("=", "");
        byte[] bytes = new byte[cleaned.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) {
                throw new IllegalArgumentException("Invalid Base32 character: " + c);
            }

            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                bytes[count++] = (byte) ((buffer >> bitsLeft) & 0xFF);
            }
        }

        return bytes;
    }
}
