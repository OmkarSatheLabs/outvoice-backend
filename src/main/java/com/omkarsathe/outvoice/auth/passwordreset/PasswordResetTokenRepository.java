package com.omkarsathe.outvoice.auth.passwordreset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
    UPDATE PasswordResetToken t
       SET t.usedAt = :now
     WHERE t.user.id = :userId
       AND t.usedAt IS NULL
       AND t.expiresAt > :now
""")
    int invalidateActiveTokens(
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
        DELETE FROM PasswordResetToken t
         WHERE t.createdAt < :cutoff
    """)
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
