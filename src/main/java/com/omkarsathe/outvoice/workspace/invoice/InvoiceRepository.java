package com.omkarsathe.outvoice.workspace.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    @Query("SELECT i FROM Invoice i JOIN FETCH i.customer c WHERE i.workspace.id = :workspaceId ORDER BY i.createdAt DESC")
    List<Invoice> findByWorkspaceIdFetchCustomer(@Param("workspaceId") UUID workspaceId);
}
