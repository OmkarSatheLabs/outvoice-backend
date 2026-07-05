package com.omkarsathe.outvoice.workspace.invitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWorkspaceInvitationRepository extends JpaRepository<UserWorkspaceInvitationEntity, UUID> {
    Optional<UserWorkspaceInvitationEntity> findByToken(String token);
    List<UserWorkspaceInvitationEntity> findByWorkspaceIdAndStatusIn(UUID workspaceId, List<String> statuses);
    @Query("""
        SELECT uwi
        FROM UserWorkspaceInvitationEntity uwi
        JOIN FETCH uwi.workspace w
        LEFT JOIN FETCH w.currency
        LEFT JOIN FETCH w.country
        JOIN FETCH uwi.invitedBy
        WHERE uwi.user.id = :userId
        AND uwi.status = "PENDING"
    """)
    List<UserWorkspaceInvitationEntity> findByUserId(UUID userId);
}
