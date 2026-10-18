package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.admin.WorkspaceBilledProjection;
import com.omkarsathe.outvoice.admin.WorkspaceCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByWorkspace_IdAndDeletedAtIsNull(UUID workspaceId);

    @Query("""
        SELECT i.workspace.id AS workspaceId,
               COUNT(i) AS total
        FROM Invoice i
        GROUP BY i.workspace.id
        """)
    List<WorkspaceCountProjection> countInvoicesByWorkspace();

    @Query("""
        SELECT i.workspace.id AS workspaceId,
               SUM(i.netTotal) AS total
        FROM Invoice i
        GROUP BY i.workspace.id
        """)
    List<WorkspaceBilledProjection> sumBilledByWorkspace();
}
