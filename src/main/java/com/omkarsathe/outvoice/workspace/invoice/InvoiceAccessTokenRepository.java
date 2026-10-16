package com.omkarsathe.outvoice.workspace.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceAccessTokenRepository extends JpaRepository<InvoiceAccessToken, UUID> {

    Optional<InvoiceAccessToken> findByTokenHash(String tokenHash);

    Optional<InvoiceAccessToken> findTopByInvoiceIdOrderByCreatedAtDesc(UUID invoiceId);
}
