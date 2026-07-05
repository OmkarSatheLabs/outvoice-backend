package com.omkarsathe.outvoice.workspace.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomRoleRepository extends JpaRepository<CustomRole, UUID> {
    List<CustomRole> findByWorkspaceId(UUID workspaceId);
}
