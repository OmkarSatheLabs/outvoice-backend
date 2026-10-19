package com.omkarsathe.outvoice.workspace.storefront;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "storefront_domains")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorefrontDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storefront_id", nullable = false)
    private Storefront storefront;

    @Column(nullable = false, unique = true)
    private String hostname;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    @Builder.Default
    private DomainVerificationStatus verificationStatus = DomainVerificationStatus.PENDING;

    @Column(name = "verification_token", nullable = false)
    private String verificationToken;

    @Column(name = "verification_token_hash", nullable = false)
    private String verificationTokenHash;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "tls_status", nullable = false, length = 50)
    @Builder.Default
    private TlsStatus tlsStatus = TlsStatus.PENDING;

    @Column(name = "tls_provider_reference")
    private String tlsProviderReference;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean isPrimary = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
