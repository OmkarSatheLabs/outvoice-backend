package com.omkarsathe.outvoice.workspace.product;

import com.omkarsathe.outvoice.storage.ObjectStorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ProductResponseMapper {

    @Autowired
    protected ObjectStorageService objectStorageService;

    @Mapping(target = "imageUrl", expression = "java(resolveImageUrl(product))")
    public abstract ProductResponse toResponse(Product product);

    protected String resolveImageUrl(Product product) {
        if (product == null) {
            return null;
        }
        if (product.getImageKey() != null && !product.getImageKey().isBlank()) {
            return objectStorageService.getUrl(product.getImageKey());
        }
        return product.getImageUrl();
    }
}
