package com.omkarsathe.outvoice.workspace;

//import com.omkarsathe.outvoice.common.entity.Auditable;
//import com.omkarsathe.outvoice.country.Country;
//import com.omkarsathe.outvoice.currency.CurrencyEntity;
//import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.workspace.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private Set<Member> members = new HashSet<>();

    // URL-friendly identifier, e.g. "outvoice" for the platform
    // super-admin workspace
//    @Column(name = "slug", nullable = false, unique = true)
//    private String slug;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "type", nullable = false, length = 20)
//    @Builder.Default
//    private WorkspaceType type = WorkspaceType.STANDARD;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "status", nullable = false, length = 10)
//    @Builder.Default
//    private WorkspaceStatus status = WorkspaceStatus.ACTIVE;

    // All amounts internal to this workspace (reports, balances) are
    // normalized to this currency; invoices can still be issued in
    // other currencies and get FX-snapshotted against this at finalization
//    @JdbcTypeCode(SqlTypes.CHAR)
//    @Column(name = "base_currency", nullable = false, length = 3)
//    private String baseCurrency;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "currency_id", updatable = false)
//    private CurrencyEntity currency;

    // False if this workspace was created as a shadow/unclaimed record
    // via WorkspaceCustomerEntity.linkedWorkspace, before anyone signed up
//    @Column(name = "is_claimed", nullable = false)
//    @Builder.Default
//    private boolean claimed = true;

    // ---- Default GST / compliance fields, used to pre-fill invoices ----

//    @Column(name = "gstin")
//    private String gstin;

//    @Column(name = "default_place_of_supply")
//    private String defaultPlaceOfSupply;

//    @Column(name = "is_gst_registered", nullable = false)
//    @Builder.Default
//    private boolean gstRegistered = false;

    // ---- Address / contact, used on invoice letterheads ----

//    @Column(name = "billing_address", columnDefinition = "TEXT")
//    private String billingAddress;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "country_id")
//    private Country country;

//    @Column(name = "support_email")
//    private String supportEmail;
//
//    @Column(name = "support_phone")
//    private String supportPhone;
//
//    @Column(name = "logo_url")
//    private String logoUrl;

    // Invoice numbering prefix, e.g. "INV" -> "INV-0042"
//    @Column(name = "invoice_number_prefix", length = 20)
//    private String invoiceNumberPrefix;
//
//    // Running counter for generating the next invoice number per workspace
//    @Column(name = "next_invoice_sequence", nullable = false)
//    @Builder.Default
//    private Long nextInvoiceSequence = 1L;
//
//    @Column(name = "is_active", nullable = false)
//    @Builder.Default
//    private boolean isActive = true;
//
//    @Column(name = "is_deleted", nullable = false)
//    @Builder.Default
//    private boolean isDeleted = false;

//    @Column(nullable = false)
//    private String taxComplianceName;
//
//    private String panNumber;
//    private String gstNumber;
//    private String tanNumber;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private WorkspaceStatus status;

//    @Column(nullable = false)
//    @Builder.Default
//    private Boolean isPlaceholder = false;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by")
//    private UserEntity createdBy;

//    @Column(updatable = false)
//    private LocalDateTime deletedAt;
}
