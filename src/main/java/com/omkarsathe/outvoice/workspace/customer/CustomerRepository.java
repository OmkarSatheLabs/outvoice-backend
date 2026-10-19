package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.workspace.invoice.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByWorkspaceId(UUID workspaceId);

    List<Customer> findAllByWorkspace_IdAndDeletedAtIsNull(UUID workspaceId);

    Optional<Customer> findByIdAndWorkspace_IdAndDeletedAtIsNull(UUID id, UUID workspaceId);

    Optional<Customer> findByIdAndWorkspace_Id(UUID id, UUID workspaceId);

    @Query("""
                SELECT DISTINCT c
                FROM Customer c
                LEFT JOIN c.invoices i
                    ON i.deletedAt IS NULL
                    AND i.status <> :status
                WHERE c.workspace.id = :workspaceId
                    AND c.deletedAt IS NULL
            """)
    List<Customer> findAllWithActiveInvoices(
            @Param("workspaceId") UUID workspaceId,
            @Param("status") InvoiceStatus status
    );
}
