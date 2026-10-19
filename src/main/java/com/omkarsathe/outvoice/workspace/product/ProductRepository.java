package com.omkarsathe.outvoice.workspace.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByWorkspace_IdAndDeletedAtIsNull(UUID workspaceId);

    Optional<Product> findByIdAndWorkspace_IdAndDeletedAtIsNull(UUID id, UUID workspaceId);

    Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

    List<Product> findAllByWorkspace_Id(UUID workspaceId);
}
