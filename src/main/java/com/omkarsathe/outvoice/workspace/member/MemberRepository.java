package com.omkarsathe.outvoice.workspace.member;

import com.omkarsathe.outvoice.admin.WorkspaceCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    List<Member> findByWorkspaceId(UUID workspaceId);

    List<Member> findByUserId(UUID userId);

    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    @Query("""
        SELECT m.workspace.id AS workspaceId,
               COUNT(m) AS total
        FROM Member m
        GROUP BY m.workspace.id
        """)
    List<WorkspaceCountProjection> countMembersByWorkspace();

    @Query("""
        SELECT m
        FROM Member m
        JOIN FETCH m.user u
        LEFT JOIN FETCH u.phoneCode pc
        WHERE m.workspace.id = :workspaceId
        ORDER BY u.fullName ASC
        """)
    List<Member> findAllByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("""
        SELECT m
        FROM Member m
        JOIN FETCH m.user u
        JOIN FETCH m.workspace w
        WHERE LOWER(u.email) = LOWER(:email)
          AND w.name = :workspaceName
        """)
    Optional<Member> findByOwnerEmailAndWorkspaceName(
            @Param("email") String email,
            @Param("workspaceName") String workspaceName
    );
}
