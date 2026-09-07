//package com.omkarsathe.outvoice.user.workspace;
//
//import com.omkarsathe.outvoice.workspace.RolePermissions;
//import com.omkarsathe.outvoice.workspace.WorkspacePrincipal;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class WorkspaceMembershipService {
//
//    private final UserWorkspaceRepository userWorkspaceRepository; // package-private, stays hidden
//
//    public Optional<WorkspacePrincipal> findPrincipal(UUID userId, UUID workspaceId) {
//        return userWorkspaceRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
//                .map(uw -> new WorkspacePrincipal(
//                        userId,
//                        workspaceId
////                        uw.getRole(),
////                        RolePermissions.forRole(uw.getRole())
//                ));
//    }
//}
