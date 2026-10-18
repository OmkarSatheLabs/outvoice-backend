package com.omkarsathe.outvoice.admin;

import com.omkarsathe.outvoice.workspace.Workspace;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminWorkspaceResponseMapper {

    AdminWorkspaceResponse toResponse(
            Workspace workspace,
            WorkspaceUsageMetrics metrics
    );
}
