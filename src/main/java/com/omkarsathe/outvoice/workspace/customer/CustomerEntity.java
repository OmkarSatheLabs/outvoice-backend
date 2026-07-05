package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.common.entity.Auditable;
import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.currency.Currency;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.workspace.WorkspaceEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column()
    private String customerName;

    @Column()
    private String companyName;

    @Column()
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_code_id")
    private PhoneCode phoneCodeId;

    @Column()
    private String mobile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @Column(name = "workspace_slug")
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    private Currency currency;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "client_user_id")
//    private UserEntity clientUser;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "client_workspace_id")
//    private WorkspaceEntity clientWorkspace;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "phone_code_id")
//    private PhoneCode phoneCode;
//
//    private String mobile;
//
//    private String companyName;
//
//    private String billingAddress;
//
//    private String taxNumber;

//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(nullable = false)
//    private LocalDateTime updatedAt;

    @Column(updatable = false)
    private LocalDateTime deletedAt;
}
