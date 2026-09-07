//package com.omkarsathe.outvoice.user.workspace.role;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//public record RoleResponse(
//
//        UUID id,
//        String name,
//        String description,
//        WorkspaceSummary workspace,
//        LocalDateTime createdAt,
//        LocalDateTime updatedAt
//
//) {
//
//    public record WorkspaceSummary(
//            UUID id,
//            String name
//    ) {}
//
//    public static RoleResponse from(RoleEntity role) {
//        return new RoleResponse(
//                role.getId(),
//                role.getName(),
//                role.getDescription(),
//                new WorkspaceSummary(
//                        role.getWorkspace().getId(),
//                        role.getWorkspace().getName()
//                ),
//                role.getCreatedAt(),
//                role.getUpdatedAt()
//        );
//    }
//}
