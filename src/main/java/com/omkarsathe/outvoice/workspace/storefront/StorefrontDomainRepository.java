package com.omkarsathe.outvoice.workspace.storefront;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StorefrontDomainRepository extends JpaRepository<StorefrontDomain, UUID> {

    Optional<StorefrontDomain> findByHostnameIgnoreCase(String hostname);

    List<StorefrontDomain> findAllByStorefrontId(UUID storefrontId);

    Optional<StorefrontDomain> findByIdAndStorefrontId(UUID id, UUID storefrontId);

    boolean existsByHostnameIgnoreCase(String hostname);

    boolean existsByHostnameIgnoreCaseAndIdNot(String hostname, UUID id);
}
