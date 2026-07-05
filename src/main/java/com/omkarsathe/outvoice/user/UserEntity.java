package com.omkarsathe.outvoice.user;

import com.omkarsathe.outvoice.common.entity.Auditable;
import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
import com.omkarsathe.outvoice.workspace.WorkspaceEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class UserEntity extends Auditable implements UserDetails {

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

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPlaceholder = false;

    @Column(name = "invited_at", updatable = false)
    private LocalDateTime invitedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private UserEntity invitedBy;

    @Column(updatable = false)
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserWorkspaceEntity> userWorkspaces = new ArrayList<>();

    // Convenience method to get workspaces directly, not mapped by JPA
    @Transient
    public List<WorkspaceEntity> getWorkspaces() {
        if (userWorkspaces == null) {
            return List.of();
        }
        return userWorkspaces.stream()
                .map(UserWorkspaceEntity::getWorkspace)
                .toList();
    }

    @Override public String getUsername() { return this.id.toString(); }
    @Override public String getPassword() { return passwordHash; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
