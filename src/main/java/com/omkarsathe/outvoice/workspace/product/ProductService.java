package com.omkarsathe.outvoice.workspace.product;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final WorkspaceRepository workspaceRepository;
    private final ProductRepository productRepository;
    private final ProductResponseMapper productResponseMapper;

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

        return productResponseMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public List<ProductResponse> getProducts(UUID workspaceId) {
        return productRepository.findAllByWorkspace_Id(workspaceId)
                .stream()
                .map(productResponseMapper::toResponse)
                .toList();
    }
}
