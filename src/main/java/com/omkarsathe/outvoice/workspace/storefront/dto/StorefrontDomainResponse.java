package com.omkarsathe.outvoice.workspace.storefront.dto;

import com.omkarsathe.outvoice.workspace.storefront.DomainVerificationStatus;
import com.omkarsathe.outvoice.workspace.storefront.TlsStatus;

import java.time.Instant;
import java.util.UUID;

public record StorefrontDomainResponse(
        UUID id,
        String hostname,
        DomainVerificationStatus verificationStatus,
        String verificationToken,
        String dnsRecordType,
        String dnsRecordHost,
        String dnsRecordValue,
        Instant verifiedAt,
        TlsStatus tlsStatus,
        boolean isPrimary,
        Instant createdAt
) {
}
