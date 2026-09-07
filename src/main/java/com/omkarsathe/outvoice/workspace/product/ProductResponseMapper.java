package com.omkarsathe.outvoice.workspace.product;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductResponseMapper {

    ProductResponse toResponse(Product product);
}
