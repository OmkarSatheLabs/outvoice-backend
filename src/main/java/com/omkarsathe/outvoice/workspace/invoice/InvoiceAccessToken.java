package com.omkarsathe.outvoice.workspace.invoice;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "invoice_access_tokens")
public class InvoiceAccessToken {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Invoice invoice;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    private Instant expiresAt;

    private boolean revoked;
}
