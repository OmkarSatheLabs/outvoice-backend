package com.omkarsathe.outvoice.workspace.payment;

import com.omkarsathe.outvoice.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "workspace_payment_settings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workspace_payment_settings_workspace_provider",
                        columnNames = {"workspace_id", "provider"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspacePaymentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean isPrimary = false;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean isEnabled = true;

    // Razorpay specific configuration
    @Column(name = "key_id")
    private String keyId;

    @Column(name = "key_secret_encrypted", length = 1000)
    private String keySecretEncrypted;

    @Column(name = "webhook_secret_encrypted", length = 1000)
    private String webhookSecretEncrypted;

    // UPI specific configuration
    @Column(name = "upi_id", length = 100)
    private String upiId;

    @Column(name = "payee_name", length = 200)
    private String payeeName;

    // Bank Transfer specific configuration
    @Column(name = "bank_account_name", length = 200)
    private String bankAccountName;

    @Column(name = "bank_account_number", length = 100)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc_code", length = 50)
    private String bankIfscCode;

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(name = "bank_branch", length = 150)
    private String bankBranch;

    @Column(name = "bank_swift_code", length = 50)
    private String bankSwiftCode;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
