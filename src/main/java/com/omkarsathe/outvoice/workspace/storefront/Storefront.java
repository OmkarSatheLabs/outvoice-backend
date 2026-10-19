package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "storefronts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Storefront {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false, unique = true)
    private Workspace workspace;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "url_strategy", nullable = false, length = 50)
    @Builder.Default
    private UrlStrategy urlStrategy = UrlStrategy.PATH_BASED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StorefrontStatus status = StorefrontStatus.DRAFT;

    @Column(name = "branding_config", columnDefinition = "TEXT")
    @Builder.Default
    private String brandingConfig = "{}";

    @Column(name = "pages_config", columnDefinition = "TEXT")
    @Builder.Default
    private String pagesConfig = "{}";

    @OneToMany(mappedBy = "storefront", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StorefrontDomain> domains = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
