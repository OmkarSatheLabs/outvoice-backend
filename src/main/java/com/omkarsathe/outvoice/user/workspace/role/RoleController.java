//package com.omkarsathe.outvoice.user.workspace.role;
//
//import com.omkarsathe.outvoice.workspace.CurrentWorkspaceUser;
//import com.omkarsathe.outvoice.workspace.Permission;
//import com.omkarsathe.outvoice.workspace.WorkspacePrincipal;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/workspaces/{workspaceId}/roles")
//@RequiredArgsConstructor
//public class RoleController {
//
//    private final RoleService roleService;
//
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public RoleResponse createRole(
//            @PathVariable UUID workspaceId,
//            @CurrentWorkspaceUser(requires = Permission.ROLES_CREATE) WorkspacePrincipal principal,
//            @Valid @RequestBody CreateRoleRequest request) {
//        return this.roleService.createRole(workspaceId, request, principal.userId());
//    }
//
//    @GetMapping
//    public List<RoleResponse> getRoles(
//            @PathVariable UUID workspaceId,
//            @CurrentWorkspaceUser(requires = Permission.ROLES_VIEW) WorkspacePrincipal principal) {
//        return this.roleService.getRoles(workspaceId);
//    }
//
//    @PutMapping("/{roleId}")
//    public RoleResponse updateRole(
//            @PathVariable UUID workspaceId,
//            @CurrentWorkspaceUser(requires = Permission.ROLES_EDIT) WorkspacePrincipal principal,
//            @PathVariable UUID roleId,
//            @Valid @RequestBody UpdateRoleRequest request) {
//        return this.roleService.updateRole(workspaceId, roleId, principal.userId(), request);
//    }
//
//    @DeleteMapping("/{roleId}")
//    public void deleteRole(
//            @PathVariable UUID workspaceId,
//            @CurrentWorkspaceUser(requires = Permission.ROLES_EDIT) WorkspacePrincipal principal,
//            @PathVariable UUID roleId) {
//        this.roleService.deleteRole(workspaceId, roleId, principal.userId());
//    }
//}
