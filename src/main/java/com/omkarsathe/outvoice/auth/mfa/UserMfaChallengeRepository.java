package com.omkarsathe.outvoice.auth.mfa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMfaChallengeRepository extends JpaRepository<UserMfaChallenge, UUID> {

    Optional<UserMfaChallenge> findBySessionTokenAndVerifiedAtIsNull(String sessionToken);

    void deleteAllByUserId(UUID userId);
}
