package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.workspace.WorkspaceResponseMapper;
import com.omkarsathe.outvoice.workspace.customer.CustomerResponseMapper;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {
                CustomerResponseMapper.class,
                WorkspaceResponseMapper.class,
        }
)
public interface InvoiceResponseMapper {

    InvoiceResponse toResponse(Invoice invoice);
}
