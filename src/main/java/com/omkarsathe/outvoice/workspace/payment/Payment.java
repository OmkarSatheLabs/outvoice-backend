package com.omkarsathe.outvoice.workspace.payment;

import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus status;

    @Column(length = 100)
    private String provider;

    @Column(name = "order_id", length = 100)
    private String orderId;

    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @Column(length = 255)
    private String signature;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_description", columnDefinition = "TEXT")
    private String errorDescription;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
