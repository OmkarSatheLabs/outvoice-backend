package com.omkarsathe.outvoice.platform;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Issues and validates JWTs for platform (internal operator) users. Uses a separate secret. */
@Component
public class PlatformJwtService {

    private static final String CLAIM_PLATFORM_ROLE = "platformRole";

    @Value("${platform.jwt.secret}")
    private String secret;

    @Value("${platform.jwt.expiry-ms:86400000}")
    private long expiryMs;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
    }

    public String generateToken(UUID platformUserId, PlatformRole role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_PLATFORM_ROLE, role.name());
        return Jwts.builder()
                .subject(platformUserId.toString())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(signingKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public PlatformContext extractContext(String token) {
        Claims claims = extractClaims(token);
        UUID id = UUID.fromString(claims.getSubject());
        PlatformRole role = PlatformRole.valueOf(claims.get(CLAIM_PLATFORM_ROLE, String.class));
        return new PlatformContext(id, role);
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
