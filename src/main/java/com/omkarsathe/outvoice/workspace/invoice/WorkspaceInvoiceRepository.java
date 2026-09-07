//package com.omkarsathe.outvoice.workspace.invoice;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//public interface WorkspaceInvoiceRepository extends JpaRepository<WorkspaceInvoiceEntity, UUID> {
//
//    @Query("SELECT i FROM WorkspaceInvoiceEntity i JOIN FETCH i.customer c WHERE i.workspace.id = :workspaceId ORDER BY i.createdAt DESC")
//    List<WorkspaceInvoiceEntity> findByWorkspaceIdFetchCustomer(@Param("workspaceId") UUID workspaceId);
//
//    @Query("SELECT i FROM WorkspaceInvoiceEntity i JOIN FETCH i.customer c WHERE i.id = :id")
//    Optional<WorkspaceInvoiceEntity> findByIdFetchCustomer(@Param("id") UUID id);
//}
