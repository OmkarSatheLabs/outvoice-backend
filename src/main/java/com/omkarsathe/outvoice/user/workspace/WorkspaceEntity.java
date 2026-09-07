//package com.omkarsathe.outvoice.user.workspace;
//
////import com.omkarsathe.outvoice.country.Country;
////import com.omkarsathe.outvoice.currency.CurrencyEntity;
////import com.omkarsathe.outvoice.user.UserEntity;
//import com.omkarsathe.outvoice.workspace.WorkspaceStatus;
//import com.omkarsathe.outvoice.workspace.WorkspaceType;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.JdbcTypeCode;
//import org.hibernate.annotations.UpdateTimestamp;
//import org.hibernate.type.SqlTypes;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "workspaces")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class WorkspaceEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @Column(name = "name", nullable = false)
//    private String name;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private WorkspaceStatus status;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "currency_id")
//    private CurrencyEntity currency;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "country_id")
//    private Country country;
//
//    @Column(name = "logo_url")
//    private String logoUrl;
//
//    @Column(name = "invoice_number_prefix", length = 20)
//    private String invoiceNumberPrefix;
//
//    @Column(name = "next_invoice_sequence", nullable = false)
//    @Builder.Default
//    private Long nextInvoiceSequence = 1L;
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by")
//    private UserEntity createdBy;
//
//    @UpdateTimestamp
//    @Column
//    private LocalDateTime updatedAt;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "updated_by")
//    private UserEntity updatedBy;
//
//    @Column
//    private LocalDateTime deletedAt;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "deleted_by")
//    private UserEntity deletedBy;
//}
