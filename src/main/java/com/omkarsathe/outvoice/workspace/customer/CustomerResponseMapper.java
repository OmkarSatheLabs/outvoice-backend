package com.omkarsathe.outvoice.workspace.customer;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerResponseMapper {

    CustomerResponse toResponse(Customer customer);
}
