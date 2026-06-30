package com.omkarsathe.outvoice.workspace;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserWorkspaceRepository extends JpaRepository<UserWorkspace, UUID> {
}
