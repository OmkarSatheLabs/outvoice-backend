//package com.omkarsathe.outvoice.workspace.invoice;
//
//import com.omkarsathe.outvoice.common.entity.Auditable;
//import com.omkarsathe.outvoice.workspace.Workspace;
//import com.omkarsathe.outvoice.workspace.customer.WorkspaceCustomerEntity;
//import jakarta.persistence.*;
//import jakarta.persistence.CascadeType;
//import jakarta.persistence.Table;
//import lombok.*;
//import org.hibernate.annotations.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//
//@Entity
//@Table(
//        name = "workspace_invoices",
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "uk_workspace_invoice_number",
//                        columnNames = {"workspace_id", "invoice_number"}
//                )
//        }
//)
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Filter(name = "deletedFilter")
//public class WorkspaceInvoiceEntity extends Auditable {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    // The biller — always a Workspace
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "workspace_id", nullable = false)
//    private Workspace workspace;
//
//    // The billee — resolved through WorkspaceCustomerEntity, which may
//    // point to a linked/claimed workspace or remain a shadow record
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "customer_id", nullable = false)
//    private WorkspaceCustomerEntity customer;
//
//    // Human-facing identifier, unique per workspace (e.g. INV-0042)
//    @Column(name = "invoice_number", nullable = false)
//    private String invoiceNumber;
////
////    @Column(name = "invoice_number", nullable = false)
////    private String invoiceNumber;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "workspace_id", nullable = false)
////    private Workspace workspace;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status", nullable = false, length = 20)
//    @Builder.Default
//    private WorkspaceInvoiceStatus status = WorkspaceInvoiceStatus.DRAFT;
//
//    @Column(name = "issue_date", nullable = false)
//    private LocalDate issueDate;
//
//    @Column(name = "due_date")
//    private LocalDate dueDate;
//
//    // ---- CurrencyEntity: locked at finalization, per your currency model ----
//
//    // CurrencyEntity this invoice is denominated in — locked once status
//    // moves out of DRAFT
//    @Column(name = "currency", nullable = false, length = 3)
//    private String currency;
//
//    // FX snapshot fields — captured at finalization time so historical
//    // invoices never drift even if workspace base_currency rates change
//    @Column(name = "fx_rate_to_base", precision = 18, scale = 8)
//    private BigDecimal fxRateToBase;
//
//    @Column(name = "base_currency_amount", precision = 18, scale = 2)
//    private BigDecimal baseCurrencyAmount;
//
//    @Column(name = "fx_rate_captured_at")
//    private java.time.Instant fxRateCapturedAt;
//
//    // ---- Amounts ----
//
//    @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
//    @Builder.Default
//    private BigDecimal subtotal = BigDecimal.ZERO;
//
//    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 2)
//    @Builder.Default
//    private BigDecimal taxAmount = BigDecimal.ZERO;
//
//    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
//    @Builder.Default
//    private BigDecimal discountAmount = BigDecimal.ZERO;
//
//    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
//    @Builder.Default
//    private BigDecimal totalAmount = BigDecimal.ZERO;
//
//    @Column(name = "amount_paid", nullable = false, precision = 18, scale = 2)
//    @Builder.Default
//    private BigDecimal amountPaid = BigDecimal.ZERO;
//
//    // ---- GST / compliance (flagged as required for foreign-currency invoices) ----
//
//    @Column(name = "gstin")
//    private String gstin;
//
//    @Column(name = "place_of_supply")
//    private String placeOfSupply;
//
//    @Column(name = "is_gst_applicable", nullable = false)
//    @Builder.Default
//    private boolean gstApplicable = false;
//
//    // ---- Misc ----
//
//    @Column(name = "notes", columnDefinition = "TEXT")
//    private String notes;
//
//    @Column(name = "terms", columnDefinition = "TEXT")
//    private String terms;
//
//    @Column(name = "finalized_at")
//    private java.time.Instant finalizedAt;
//
//    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @Builder.Default
//    private List<WorkspaceInvoiceLineItemEntity> lineItems = new java.util.ArrayList<>();
//
//    @Column(name = "is_deleted", nullable = false)
//    @Builder.Default
//    private boolean isDeleted = false;
//
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "customer_id", nullable = false)
////    private UserEntity customer;
////
////    @Column(nullable = false, precision = 15, scale = 2)
////    private BigDecimal amount;
////
////    @Column(name = "currency_code", nullable = false, length = 3)
////    private String currencyCode;
////
////    @Enumerated(EnumType.STRING)
////    @Column(nullable = false, length = 20)
////    @Builder.Default
////    private WorkspaceInvoiceStatus status = WorkspaceInvoiceStatus.DRAFT;
////
////    @Column(name = "issue_date", nullable = false)
////    private LocalDate issueDate;
////
////    @Column(name = "due_date", nullable = false)
////    private LocalDate dueDate;
////
////    @Column(name = "last_reminder_sent_at")
////    private LocalDateTime lastReminderSentAt;
////
////    @Column(name = "next_reminder_scheduled_at")
////    private LocalDate nextReminderScheduledAt;
////
////    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
////    @Builder.Default
////    private BigDecimal taxRate = BigDecimal.ZERO;
////
////    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
////    @Builder.Default
////    private List<WorkspaceInvoiceLineItemEntity> lineItems = new ArrayList<>();
////
////    @CreationTimestamp
////    @Column(name = "created_at", nullable = false, updatable = false)
////    private LocalDateTime createdAt;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "created_by", nullable = false, updatable = false)
////    private UserEntity createdBy;
////
////    @UpdateTimestamp
////    @Column(name = "updated_at", nullable = false)
////    private LocalDateTime updatedAt;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "updated_by", nullable = false)
////    private UserEntity updatedBy;
////
//    public void addLineItem(WorkspaceInvoiceLineItemEntity item) {
//        lineItems.add(item);
//        item.setInvoice(this);
//    }
//}
