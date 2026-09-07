//package com.omkarsathe.outvoice.user.workspace;
//
//import com.omkarsathe.outvoice.phone.PhoneCode;
////import com.omkarsathe.outvoice.user.dto.CreateUserRequestDto;
////import com.omkarsathe.outvoice.user.workspace.dto.CreateUserWorkspaceRequestDto;
//import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class UserWorkspaceService {
//
//    private final UserWorkspaceRepository userWorkspaceRepository;
//
////    public UserWorkspaceEntity save(CreateUserWorkspaceRequestDto request) {
////        UserWorkspaceEntity userWorkspace = new UserWorkspaceEntity();
////        userWorkspace.setUser(request.getUser());
////        userWorkspace.setWorkspace(request.getWorkspace());
//////        userWorkspace.setRole(request.getRole());
////        userWorkspace.setIsDefaultWorkspace(true);
////        userWorkspace.setStatus(MemberStatusEnum.ACTIVE);
////        return userWorkspaceRepository.save(userWorkspace);
////    }
//
////    public List<UserWorkspaceEntity> findByUserId(UUID id) {
////        List<UserWorkspaceEntity> workspaces = userWorkspaceRepository.findByUserId(id);
////        if (workspaces.isEmpty()) {
////            throw new RuntimeException("No workspaces found for user " + id);
////        }
////        return workspaces;
////    }
////
////    public UserWorkspaceEntity findById(UUID id) {
////        return userWorkspaceRepository.findById(id).orElseThrow(() -> new RuntimeException("User workspace with UUID " + id + " not found"));
////    }
//
//    public void assertUserBelongsToWorkspace(UUID userId, UUID workspaceId) {
//        boolean isAssociated = userWorkspaceRepository
//                .existsByUser_IdAndWorkspace_Id(userId, workspaceId);
//
//        if (!isAssociated) {
//            throw new AccessDeniedException(
//                    "User " + userId + " is not associated with workspace " + workspaceId);
//        }
//    }
//}
