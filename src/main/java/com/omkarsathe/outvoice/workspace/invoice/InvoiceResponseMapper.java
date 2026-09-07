package com.omkarsathe.outvoice.workspace.invoice;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvoiceResponseMapper {

    InvoiceResponse toResponse(Invoice invoice);
}
