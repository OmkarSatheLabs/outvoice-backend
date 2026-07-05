package com.omkarsathe.outvoice.user.workspace;

import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserWorkspaceRepository extends JpaRepository<UserWorkspaceEntity, UUID> {
    Optional<UserWorkspaceEntity> findByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);

    @Query("SELECT uw FROM UserWorkspaceEntity uw JOIN FETCH uw.workspace w LEFT JOIN FETCH w.currency LEFT JOIN FETCH w.country WHERE uw.user.id = :userId AND uw.status = :status")
    List<UserWorkspaceEntity> findByUserIdAndStatusFetchWorkspace(@Param("userId") UUID userId, @Param("status") MemberStatusEnum status);

    @Query("SELECT uw FROM UserWorkspaceEntity uw JOIN FETCH uw.user u WHERE uw.workspace.id = :workspaceId")
    List<UserWorkspaceEntity> findByWorkspaceId(@Param("workspaceId") UUID workspaceId);
    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
}
