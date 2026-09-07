//package com.omkarsathe.outvoice.user;
//
//import com.omkarsathe.outvoice.common.entity.Auditable;
//import com.omkarsathe.outvoice.common.enums.EntityStatus;
//import com.omkarsathe.outvoice.country.Country;
//import com.omkarsathe.outvoice.phone.PhoneCode;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.time.LocalDateTime;
//import java.util.Collection;
//import java.util.List;
//import java.util.UUID;
//
//@Entity
//@Table(name = "users")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class UserEntity extends Auditable implements UserDetails {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @Column(unique = true)
//    private String email;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "phone_code_id")
//    private PhoneCode phoneCode;
//
//    @Column()
//    private String mobile;
//
//    @Column(nullable = false)
//    private String fullName;
//
//    @Column(nullable = false)
//    private String passwordHash;
//
//    @Column(nullable = false)
//    private Boolean isEmailVerified;
//
//    @Column(nullable = false)
//    private Boolean isMobileVerified;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "country_id")
//    private Country country;
//
//    @Column(nullable = false)
//    private EntityStatus status;
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(nullable = false)
//    private LocalDateTime updatedAt;
//
//    @Column()
//    private LocalDateTime deletedAt;
//
////    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
////    @Builder.Default
////    private List<UserWorkspaceEntity> userWorkspaces = new ArrayList<>();
//
//    // Convenience method to get workspaces directly, not mapped by JPA
////    @Transient
////    public List<Workspace> getWorkspaces() {
////        if (userWorkspaces == null) {
////            return List.of();
////        }
////        return userWorkspaces.stream()
////                .map(UserWorkspaceEntity::getWorkspace)
////                .toList();
////    }
//
//    @Override public String getUsername() { return this.id.toString(); }
//    @Override public String getPassword() { return passwordHash; }
//    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
////    @Override public boolean isAccountNonExpired() { return true; }
////    @Override public boolean isAccountNonLocked() { return true; }
////    @Override public boolean isCredentialsNonExpired() { return true; }
////    @Override public boolean isEnabled() { return true; }
//}
