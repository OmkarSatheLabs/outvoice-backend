package com.omkarsathe.outvoice.workspace.invoice.item;

import com.omkarsathe.outvoice.workspace.WorkspaceResponseMapper;
import com.omkarsathe.outvoice.workspace.customer.CustomerResponseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        WorkspaceResponseMapper.class,
        CustomerResponseMapper.class,
        ItemResponseMapper.class
})
public interface ItemResponseMapper {

    ItemResponse toResponse(Item item);
}
