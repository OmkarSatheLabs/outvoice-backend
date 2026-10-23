package com.omkarsathe.outvoice.workspace;

import com.omkarsathe.outvoice.workspace.customer.Customer;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "workspaces")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(
            mappedBy = "workspace",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<Member> members = new HashSet<>();

    // Invoice numbering prefix, e.g. "INV" -> "INV-0042"
    @Column(name = "invoice_number_prefix", length = 20)
    private String invoiceNumberPrefix;

    // Running counter for generating the next invoice number per workspace
    @Column(name = "next_invoice_sequence", nullable = false)
    @Builder.Default
    private Long nextInvoiceSequence = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private WorkspaceStatus status = WorkspaceStatus.ACTIVE;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Customer> customers = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = WorkspaceStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isSuspended() {
        return WorkspaceStatus.SUSPENDED.equals(this.status);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
