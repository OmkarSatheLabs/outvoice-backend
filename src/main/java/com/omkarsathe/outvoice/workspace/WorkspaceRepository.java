package com.omkarsathe.outvoice.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, UUID> {

    Optional<WorkspaceEntity> findBySlug(String slug);

    @Query("SELECT w FROM WorkspaceEntity w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(w.slug) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<WorkspaceEntity> searchWorkspaces(@Param("query") String query);
}
