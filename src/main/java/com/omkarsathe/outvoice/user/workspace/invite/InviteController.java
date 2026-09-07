//package com.omkarsathe.outvoice.user.workspace.invite;
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
//@RequestMapping("/api/workspaces/{workspaceId}/invites")
//@RequiredArgsConstructor
//public class InviteController {
//
//    private final InviteService inviteService;
//
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public InviteResponse createInvite(
//            @PathVariable UUID workspaceId,
//            @CurrentWorkspaceUser(requires = Permission.INVITES_CREATE) WorkspacePrincipal principal,
//            @Valid @RequestBody CreateInviteRequest request) {
//        return this.inviteService.createInvite(workspaceId, request, principal.userId());
//    }
//
//    @GetMapping
//    public List<InviteResponse> getInvites(
//            @PathVariable UUID workspaceId,
//            @CurrentWorkspaceUser(requires = Permission.INVITES_VIEW) WorkspacePrincipal principal) {
//        return this.inviteService.getInvites(workspaceId);
//    }
//}
