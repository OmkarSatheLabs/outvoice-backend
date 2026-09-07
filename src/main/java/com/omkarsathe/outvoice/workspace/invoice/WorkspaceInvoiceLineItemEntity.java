//package com.omkarsathe.outvoice.workspace.invoice;
//
//import com.omkarsathe.outvoice.common.entity.Auditable;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.Filter;
//import org.hibernate.annotations.FilterDef;
//import org.hibernate.annotations.ParamDef;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "workspace_invoice_line_items")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Filter(name = "deletedFilter")
//public class WorkspaceInvoiceLineItemEntity extends Auditable {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "invoice_id", nullable = false)
//    private WorkspaceInvoiceEntity invoice;
//
//    // Preserves the order in which line items appear on the invoice
//    @Column(name = "sort_order", nullable = false)
//    @Builder.Default
//    private Integer sortOrder = 0;
//
//    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
//    private String description;
//
//    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
//    @Builder.Default
//    private BigDecimal quantity = BigDecimal.ONE;
//
//    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
//    private BigDecimal unitPrice;
//
//    // quantity * unit_price, before tax/discount — stored, not computed on read
//    @Column(name = "line_subtotal", nullable = false, precision = 18, scale = 2)
//    private BigDecimal lineSubtotal;
//
//    @Column(name = "tax_rate", precision = 5, scale = 2)
//    private BigDecimal taxRate;
//
//    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 2)
//    @Builder.Default
//    private BigDecimal taxAmount = BigDecimal.ZERO;
//
//    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
//    @Builder.Default
//    private BigDecimal discountAmount = BigDecimal.ZERO;
//
//    // line_subtotal + tax_amount - discount_amount
//    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
//    private BigDecimal lineTotal;
//
//    // HSN/SAC code — relevant for GST-applicable invoices
//    @Column(name = "hsn_sac_code", length = 20)
//    private String hsnSacCode;
//
//    @Column(name = "is_deleted", nullable = false)
//    @Builder.Default
//    private boolean isDeleted = false;
//
////    @Id
////    @GeneratedValue(strategy = GenerationType.UUID)
////    private UUID id;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "invoice_id", nullable = false)
////    private WorkspaceInvoiceEntity invoice;
////
////    @Column(nullable = false)
////    private String description;
////
////    @Column(nullable = false)
////    @Builder.Default
////    private Integer quantity = 1;
////
////    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
////    private BigDecimal unitPrice;
////
////    @Column(nullable = false, precision = 15, scale = 2)
////    private BigDecimal amount;
////
////    @CreationTimestamp
////    @Column(name = "created_at", nullable = false, updatable = false)
////    private LocalDateTime createdAt;
//}
