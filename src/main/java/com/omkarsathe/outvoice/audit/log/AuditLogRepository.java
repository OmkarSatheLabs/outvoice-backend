package com.omkarsathe.outvoice.audit.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    long countByWorkspaceId(UUID workspaceId);

    long countByWorkspaceIdAndChangedAtAfter(UUID workspaceId, Instant after);

    @Query("SELECT a.resourceType, COUNT(a) FROM AuditLog a WHERE a.workspaceId = :workspaceId GROUP BY a.resourceType")
    List<Object[]> countByResourceTypeGrouped(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.workspaceId = :workspaceId GROUP BY a.action")
    List<Object[]> countByActionGrouped(@Param("workspaceId") UUID workspaceId);

    List<AuditLog> findTop20ByWorkspaceIdOrderByChangedAtDesc(UUID workspaceId);
}
