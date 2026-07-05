package com.omkarsathe.outvoice.workspace;

import com.omkarsathe.outvoice.common.entity.Auditable;
import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.currency.Currency;
import com.omkarsathe.outvoice.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workspaces")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceEntity extends Auditable {

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
//
//    private String panNumber;
//    private String gstNumber;
//    private String tanNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPlaceholder = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @Column(updatable = false)
    private LocalDateTime deletedAt;
}
