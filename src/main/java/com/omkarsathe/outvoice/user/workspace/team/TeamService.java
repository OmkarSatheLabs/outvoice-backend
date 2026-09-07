//package com.omkarsathe.outvoice.user.workspace.team;
//
//import com.omkarsathe.outvoice.user.UserService;
//import com.omkarsathe.outvoice.workspace.WorkspaceService;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class TeamService {
//
//    private final TeamRepository teamRepository;
//
//    private final WorkspaceService workspaceService;
//    private final UserService userService;
//
//    @Transactional
//    public TeamResponse createTeam(UUID workspaceId, CreateTeamRequest request, UUID createdBy) {
//        ifExistsByNameIgnoreCaseThrows(workspaceId, request.name(), null);
//
//        TeamEntity team = new TeamEntity();
//        team.setName(request.name());
//        team.setDescription(request.description());
////        team.setWorkspace(workspaceService.findById(workspaceId));
//        team.setCreatedBy(userService.findById(createdBy));
//
//        TeamEntity saved = teamRepository.save(team);
//
//        return TeamResponse.from(saved);
//    }
//
//    private void ifExistsByNameIgnoreCaseThrows(UUID workspaceId, String name, UUID excludeTeamId) {
//        boolean duplicate = excludeTeamId == null
//                ? teamRepository.existsByNameIgnoreCaseAndWorkspaceIdAndDeletedAtIsNull(name, workspaceId)
//                : teamRepository.existsByNameIgnoreCaseAndWorkspaceIdAndIdNotAndDeletedAtIsNull(name, workspaceId, excludeTeamId);
//        if (duplicate) {
//            throw new DuplicateTeamException(name);
//        }
//    }
//
//    @Transactional
//    public List<TeamResponse> getTeams(UUID workspaceId) {
//        List<TeamEntity> teams = teamRepository.findAllByWorkspaceIdAndDeletedAtIsNull(workspaceId);
//
//        List<TeamResponse> teamResponses = new ArrayList<>();
//
//        for (TeamEntity team : teams) {
//            teamResponses.add(TeamResponse.from(team));
//        }
//
//        return teamResponses;
//    }
//
//    @Transactional
//    public TeamResponse updateTeam(UUID workspaceId, UUID teamId, UUID updatedBy, UpdateTeamRequest request) {
//        ifExistsByNameIgnoreCaseThrows(workspaceId, request.name(), teamId);
//
//        TeamEntity team = teamRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(teamId, workspaceId)
//                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
//
//        team.setName(request.name());
//        team.setDescription(request.description());
//        team.setUpdatedAt(LocalDateTime.now());
//        team.setUpdatedBy(userService.findById(updatedBy));
//
//        TeamEntity saved = teamRepository.save(team);
//
//        return TeamResponse.from(saved);
//    }
//
//    public void deleteTeam(UUID workspaceId, UUID teamId, UUID deletedBy) {
//        TeamEntity team = teamRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(teamId, workspaceId)
//                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
//
//        team.setDeletedAt(LocalDateTime.now());
//        team.setDeletedBy(userService.findById(deletedBy));
//
//        teamRepository.save(team);
//    }
//}
