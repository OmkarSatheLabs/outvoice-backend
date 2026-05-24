package com.omkarsathe.outvoice.organization;

import com.omkarsathe.outvoice.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrganization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgRole role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefaultOrg = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;
}
