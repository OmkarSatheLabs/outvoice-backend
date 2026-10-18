package com.omkarsathe.outvoice.workspace.product;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final WorkspaceRepository workspaceRepository;
    private final ProductRepository productRepository;
    private final ProductResponseMapper productResponseMapper;
    private final AuditService auditService;

    public ProductResponse create(UUID workspaceId, CreateProduct request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workspace not found: " + workspaceId
                        ));

        Product product = Product.builder()
                .workspace(workspace)
                .name(request.name())
                .stock(request.stock())
                .unit(request.unit())
                .price(request.price())
                .build();

        Product saved = productRepository.save(product);

        auditService.log(
                workspaceId,
                AuditAction.CREATE,
                AuditResourceType.PRODUCT,
                saved.getId(),
                "Created product " + saved.getName(),
                Map.of(
                        "name", saved.getName() != null ? saved.getName() : "",
                        "price", saved.getPrice() != null ? saved.getPrice() : BigDecimal.ZERO,
                        "stock", saved.getStock(),
                        "unit", saved.getUnit() != null ? saved.getUnit() : ""
                )
        );

        return productResponseMapper.toResponse(saved);
    }

    @Transactional
    public List<ProductResponse> getProducts(UUID workspaceId) {
        return productRepository.findAllByWorkspace_Id(workspaceId)
                .stream()
                .map(productResponseMapper::toResponse)
                .toList();
    }
}
