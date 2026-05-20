package com.omkarsathe.outvoice.platform;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlatformUserRepository extends JpaRepository<PlatformUser, UUID> {
    Optional<PlatformUser> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(PlatformRole role);
}
