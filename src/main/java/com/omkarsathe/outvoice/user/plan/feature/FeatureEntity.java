//package com.omkarsathe.outvoice.user.plan.feature;
//
////import com.omkarsathe.outvoice.user.UserEntity;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "features")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class FeatureEntity {
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
