package com.omkarsathe.outvoice.country;

import com.omkarsathe.outvoice.currency.Currency;
import com.omkarsathe.outvoice.entity.CountryPhoneCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;            // e.g. India

    @Column(nullable = false, unique = true, length = 2, name = "iso_code_2")
    private String isoCode2;        // e.g. IN

    @Column(nullable = false, unique = true, length = 3, name = "iso_code_3")
    private String isoCode3;        // e.g. IND

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_currency_id")
    private Currency defaultCurrency;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Convenience: all phone code mappings for this country
    // Use .stream().filter(CountryPhoneCode::getIsPrimary) to get the primary one
    @OneToMany(mappedBy = "country", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CountryPhoneCode> phoneCodeMappings = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
