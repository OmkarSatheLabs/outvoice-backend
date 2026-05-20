package com.omkarsathe.outvoice.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omkarsathe.outvoice.organization.OrgRole;
import com.omkarsathe.outvoice.organization.Permission;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Issues and validates JWT tokens for org-layer users.
 * Tokens carry org context claims (orgId, role, permissions) after /auth/select-org.
 */
@Component
@Slf4j
public class JwtService {

    private static final String CLAIM_ORG_ID = "orgId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_PERMISSIONS = "permissions";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiry-ms}")
    private long expiryMs;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
    }

    /** Issues a user-level JWT with no org context (returned after login with multiple orgs). */
    public String generateUserToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(signingKey())
                .compact();
    }

    /** Issues a fully scoped JWT after org selection. */
    public String generateOrgToken(UUID userId, UUID orgId, OrgRole role, List<Permission> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ORG_ID, orgId.toString());
        claims.put(CLAIM_ROLE, role.name());
        if (permissions != null && !permissions.isEmpty()) {
            claims.put(CLAIM_PERMISSIONS, permissions.stream().map(Permission::name).toList());
        }
        return Jwts.builder()
                .subject(userId.toString())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(signingKey())
                .compact();
    }

    /** Extracts all claims from a token. Throws JwtException if invalid. */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UserContext extractUserContext(String token) {
        Claims claims = extractClaims(token);
        UUID userId = UUID.fromString(claims.getSubject());

        String orgIdStr = claims.get(CLAIM_ORG_ID, String.class);
        String roleStr = claims.get(CLAIM_ROLE, String.class);

        UUID orgId = orgIdStr != null ? UUID.fromString(orgIdStr) : null;
        OrgRole role = roleStr != null ? OrgRole.valueOf(roleStr) : null;

        List<Permission> permissions = List.of();
        Object rawPerms = claims.get(CLAIM_PERMISSIONS);
        if (rawPerms instanceof List<?> rawList) {
            permissions = MAPPER.convertValue(rawList, new TypeReference<List<Permission>>() {});
        }

        return new UserContext(userId, orgId, role, permissions);
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Legacy helper — kept for UserDetailsService compatibility. */
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }
}
