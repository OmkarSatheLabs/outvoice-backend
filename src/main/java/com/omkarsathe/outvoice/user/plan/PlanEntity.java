//package com.omkarsathe.outvoice.user.plan;
//
//import com.omkarsathe.outvoice.user.UserEntity;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "plans")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class PlanEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @Column(nullable = false)
//    private String name;
//
//    @Column
//    private String description;
//
//    @Column(precision = 10, scale = 2, nullable = false)
//    private BigDecimal price;
//
//    @Column(length = 20, nullable = false)
//    private String billingCycle;
//
//    @Column(nullable = false)
//    private boolean isActive = true;
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
