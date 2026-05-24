package com.omkarsathe.outvoice.organization;

import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.currency.Currency;
import com.omkarsathe.outvoice.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @Column(nullable = false)
    private String taxComplianceName;

    private String panNumber;
    private String gstNumber;
    private String tanNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @Column(nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(updatable = false)
    private LocalDateTime deletedAt;

    @PreRemove
    void preRemove() {
        deletedAt = LocalDateTime.now();
    }
}
