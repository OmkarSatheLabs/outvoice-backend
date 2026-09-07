//package com.omkarsathe.outvoice.user.workspace.team;
//
//import com.omkarsathe.outvoice.workspace.CurrentWorkspaceUser;
//import com.omkarsathe.outvoice.workspace.Permission;
////import com.omkarsathe.outvoice.workspace.WorkspacePrincipal;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/workspaces/{workspaceId}/teams")
//@RequiredArgsConstructor
//public class TeamController {
//
//    private final TeamService teamService;
//
////    @PostMapping
////    @ResponseStatus(HttpStatus.CREATED)
////    public TeamResponse createTeam(
////            @PathVariable UUID workspaceId,
////            @CurrentWorkspaceUser(requires = Permission.TEAMS_CREATE) WorkspacePrincipal principal,
////            @Valid @RequestBody CreateTeamRequest request) {
////        return this.teamService.createTeam(workspaceId, request, principal.userId());
////    }
//
////    @GetMapping
////    public List<TeamResponse> getTeams(
////            @PathVariable UUID workspaceId,
////            @CurrentWorkspaceUser(requires = Permission.TEAMS_VIEW) WorkspacePrincipal principal) {
////        return this.teamService.getTeams(workspaceId);
////    }
//
////    @PutMapping("/{teamId}")
////    public TeamResponse updateTeam(
////            @PathVariable UUID workspaceId,
////            @CurrentWorkspaceUser(requires = Permission.TEAMS_EDIT) WorkspacePrincipal principal,
////            @PathVariable UUID teamId,
////            @Valid @RequestBody UpdateTeamRequest request) {
////        return this.teamService.updateTeam(workspaceId, teamId, principal.userId(), request);
////    }
//
////    @DeleteMapping("/{teamId}")
////    public void deleteTeam(
////            @PathVariable UUID workspaceId,
////            @CurrentWorkspaceUser(requires = Permission.TEAMS_EDIT) WorkspacePrincipal principal,
////            @PathVariable UUID teamId) {
////        this.teamService.deleteTeam(workspaceId, teamId, principal.userId());
////    }
//}
