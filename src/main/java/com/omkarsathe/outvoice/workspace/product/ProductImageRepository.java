package com.omkarsathe.outvoice.workspace.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findAllByProduct_IdOrderByDisplayOrderAscCreatedAtAsc(UUID productId);

    Optional<ProductImage> findByProduct_IdAndIsPrimaryTrue(UUID productId);

    void deleteAllByProduct_Id(UUID productId);
}
