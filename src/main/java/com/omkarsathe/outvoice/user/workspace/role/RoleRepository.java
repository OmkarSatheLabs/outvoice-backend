//package com.omkarsathe.outvoice.user.workspace.role;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
//    List<RoleEntity> findAllByWorkspaceIdAndDeletedAtIsNull(UUID workspaceId);
//
//    Optional<RoleEntity> findByIdAndWorkspaceIdAndDeletedAtIsNull(UUID id, UUID workspaceId);
//
//    boolean existsByNameIgnoreCaseAndWorkspaceIdAndDeletedAtIsNull(String name, UUID workspaceId);
//    boolean existsByNameIgnoreCaseAndWorkspaceIdAndIdNotAndDeletedAtIsNull(String name, UUID workspaceId, UUID excludeId);
//}
