package com.omkarsathe.outvoice.admin;

import com.omkarsathe.outvoice.workspace.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminWorkspaceResponseMapper {

    @Mapping(target = "id", source = "workspace.id")
    @Mapping(target = "name", source = "workspace.name")
    @Mapping(target = "invoiceNumberPrefix", source = "workspace.invoiceNumberPrefix")
    @Mapping(target = "status", source = "workspace.status")
    @Mapping(target = "suspended", expression = "java(workspace.isSuspended())")
    @Mapping(target = "createdAt", source = "workspace.createdAt")
    @Mapping(target = "metrics", source = "metrics")
    AdminWorkspaceResponse toResponse(
            Workspace workspace,
            WorkspaceUsageMetrics metrics
    );
}
