package com.omkarsathe.outvoice.entity;

import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.phone.PhoneCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "country_phone_codes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_country_phone_code",
                columnNames = {"country_id", "phone_code_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryPhoneCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_code_id", nullable = false)
    private PhoneCode phoneCode;

    // The partial unique index (WHERE is_primary = TRUE) in the DDL ensures
    // only one primary code per country at the DB level.
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
