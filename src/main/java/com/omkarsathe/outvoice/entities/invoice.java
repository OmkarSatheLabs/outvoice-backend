//package com.omkarsathe.outvoice.entities;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//@Entity
//@Table(name = "invoices")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class invoice {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "workspace_id")
//    private UUID workspace;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "customer_id")
//    private UUID customer;
//
//    @Column()
//    private BigDecimal total;
//
//    @Column()
//    private BigDecimal tax;
//
//    @Column()
//    private BigDecimal discount;
//
//    @Column()
//    private BigDecimal netTotal;
//}
