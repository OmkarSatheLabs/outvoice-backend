//package com.omkarsathe.outvoice.user.workspace.team;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//public record TeamResponse(
//
//        UUID id,
//        String name,
//        String description,
//        TeamResponse.WorkspaceSummary workspace,
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
//    public static TeamResponse from(TeamEntity team) {
//        return new TeamResponse(
//                team.getId(),
//                team.getName(),
//                team.getDescription(),
//                new TeamResponse.WorkspaceSummary(
//                        team.getWorkspace().getId(),
//                        team.getWorkspace().getName()
//                ),
//                team.getCreatedAt(),
//                team.getUpdatedAt()
//        );
//    }
//}
