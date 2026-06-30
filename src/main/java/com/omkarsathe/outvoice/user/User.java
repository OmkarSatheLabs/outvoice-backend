package com.omkarsathe.outvoice.user;

import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.phone.PhoneCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "users_mobile_key",
                        columnNames = {
                                "phone_code_id",
                                "mobile"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_code_id")
    private PhoneCode phoneCode;

    @Column()
    private String mobile;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean isEmailVerified;

    @Column(nullable = false)
    private Boolean isMobileVerified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(updatable = false)
    private LocalDateTime deletedAt;

//    @PreRemove
//    void preRemove() {
//        deletedAt = LocalDateTime.now();
//    }

    // The JWT subject — email takes priority over mobile
//    public String getPrincipal() {
//        return email != null ? email : mobile;
//    }

    @Override public String getUsername() { return this.id.toString(); }
    @Override public String getPassword() { return passwordHash; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
