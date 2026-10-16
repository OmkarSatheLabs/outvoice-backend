package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceAccessTokenService {

    private final InvoiceAccessTokenRepository tokenRepository;

    @Value("${frontend.url:http://localhost:4200}")
    private String frontendUrl;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public InvoiceAccessToken getOrCreateToken(Invoice invoice) {
        Optional<InvoiceAccessToken> existingOpt = tokenRepository.findTopByInvoiceIdOrderByCreatedAtDesc(invoice.getId());
        if (existingOpt.isPresent() && existingOpt.get().isValid()) {
            return existingOpt.get();
        }

        // Generate unguessable 256-bit secure hex token
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String tokenString = HexFormat.of().formatHex(bytes);

        // Tokens valid for 90 days by default
        Instant expiresAt = Instant.now().plus(90, ChronoUnit.DAYS);

        InvoiceAccessToken token = InvoiceAccessToken.builder()
                .invoice(invoice)
                .tokenHash(tokenString)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        return tokenRepository.save(token);
    }

    @Transactional
    public Invoice resolveInvoiceByToken(String tokenString) {
        InvoiceAccessToken token = tokenRepository.findByTokenHash(tokenString)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invoice access link"));

        if (!token.isValid()) {
            throw new ResourceNotFoundException("This invoice access link has expired or been revoked");
        }

        token.setLastAccessedAt(Instant.now());
        tokenRepository.save(token);

        return token.getInvoice();
    }

    public String buildPublicInvoiceUrl(String tokenString) {
        return frontendUrl.replaceAll("/+$", "") + "/view/invoice/" + tokenString;
    }
}
