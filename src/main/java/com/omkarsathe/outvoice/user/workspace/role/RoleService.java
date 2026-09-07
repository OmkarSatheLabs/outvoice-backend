//package com.omkarsathe.outvoice.user.workspace.role;
//
//import com.omkarsathe.outvoice.user.UserService;
//import com.omkarsathe.outvoice.workspace.WorkspaceService;
//import jakarta.transaction.Transactional;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
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
//public class RoleService {
//    private final RoleRepository roleRepository;
//    private final WorkspaceService workspaceService;
//    private final UserService userService;
//
//    @Transactional
//    public RoleResponse createRole(UUID workspaceId, CreateRoleRequest request, UUID createdBy) {
//        ifExistsByNameIgnoreCaseThrows(workspaceId, request.name(), null);
//
//        RoleEntity role = new RoleEntity();
//        role.setName(request.name());
//        role.setDescription(request.description());
////        role.setWorkspace(workspaceService.findById(workspaceId));
//        role.setCreatedBy(userService.findById(createdBy));
//
//        RoleEntity saved = roleRepository.save(role);
//
//        return RoleResponse.from(saved);
//    }
//
//    private void ifExistsByNameIgnoreCaseThrows(UUID workspaceId, String name, UUID excludeRoleId) {
//        boolean duplicate = excludeRoleId == null
//                ? roleRepository.existsByNameIgnoreCaseAndWorkspaceIdAndDeletedAtIsNull(name, workspaceId)
//                : roleRepository.existsByNameIgnoreCaseAndWorkspaceIdAndIdNotAndDeletedAtIsNull(name, workspaceId, excludeRoleId);
//        if (duplicate) {
//            throw new DuplicateRoleException(name);
//        }
//    }
//
//    @Transactional
//    public List<RoleResponse> getRoles(UUID workspaceId) {
//        List<RoleEntity> roles = roleRepository.findAllByWorkspaceIdAndDeletedAtIsNull(workspaceId);
//
//        List<RoleResponse> roleResponses = new ArrayList<>();
//
//        for (RoleEntity role : roles) {
//            roleResponses.add(RoleResponse.from(role));
//        }
//
//        return roleResponses;
//    }
//
//    @Transactional
//    public RoleResponse updateRole(UUID workspaceId, UUID roleId, UUID updatedBy, UpdateRoleRequest request) {
//        ifExistsByNameIgnoreCaseThrows(workspaceId, request.name(), roleId);
//
//        RoleEntity role = roleRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(roleId, workspaceId)
//                .orElseThrow(() -> new RoleNotFoundException(roleId.toString()));
//
//        role.setName(request.name());
//        role.setDescription(request.description());
//        role.setUpdatedAt(LocalDateTime.now());
//        role.setUpdatedBy(userService.findById(updatedBy));
//
//        RoleEntity saved = roleRepository.save(role);
//
//        return RoleResponse.from(saved);
//    }
//
//    public void deleteRole(UUID workspaceId, UUID roleId, UUID deletedBy) {
//        RoleEntity role = roleRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(roleId, workspaceId)
//                .orElseThrow(() -> new RoleNotFoundException(roleId.toString()));
//
//        role.setDeletedAt(LocalDateTime.now());
//        role.setDeletedBy(userService.findById(deletedBy));
//
//        roleRepository.save(role);
//    }
//}
