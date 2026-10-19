package com.omkarsathe.outvoice.workspace.storefront;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StorefrontRepository extends JpaRepository<Storefront, UUID> {

    Optional<Storefront> findByWorkspaceId(UUID workspaceId);

    Optional<Storefront> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndWorkspaceIdNot(String slug, UUID workspaceId);
}
