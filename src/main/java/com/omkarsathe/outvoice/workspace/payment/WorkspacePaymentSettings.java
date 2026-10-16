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
    @Builder.Default
    private String provider = "RAZORPAY";

    @Column(name = "key_id", nullable = false)
    private String keyId;

    @Column(name = "key_secret_encrypted", nullable = false, length = 1000)
    private String keySecretEncrypted;

    @Column(name = "webhook_secret_encrypted", length = 1000)
    private String webhookSecretEncrypted;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean isEnabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
