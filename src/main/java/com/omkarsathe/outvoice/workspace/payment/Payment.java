package com.omkarsathe.outvoice.workspace.payment;

import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Invoice invoice;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String provider;

    private String providerPaymentId;

    private Instant createdAt;

    private Instant completedAt;
}
