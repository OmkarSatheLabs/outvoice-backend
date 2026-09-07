package com.omkarsathe.outvoice.workspace;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkspaceResponseMapper {

    WorkspaceResponse toResponse(Workspace workspace);
}
