package com.omkarsathe.outvoice.currency;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "currencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 3)
    private String code;                // ISO 4217 e.g. INR, USD

    @Column(nullable = false, length = 100)
    private String name;                // e.g. Indian Rupee

    @Column(nullable = false, length = 10)
    private String symbol;              // e.g. ₹, $

    @Column(nullable = false)
    @Builder.Default
    private Short decimalPlaces = 2;  // 0 for JPY, 3 for KWD

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
