package com.omkarsathe.outvoice.workspace.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspacePaymentSettingsRepository extends JpaRepository<WorkspacePaymentSettings, UUID> {

    Optional<WorkspacePaymentSettings> findByWorkspaceIdAndProvider(UUID workspaceId, String provider);

    List<WorkspacePaymentSettings> findByWorkspaceId(UUID workspaceId);

    List<WorkspacePaymentSettings> findByWorkspaceIdAndIsEnabledTrue(UUID workspaceId);

    Optional<WorkspacePaymentSettings> findByWorkspaceIdAndIsPrimaryTrue(UUID workspaceId);
}
