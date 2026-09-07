//package com.omkarsathe.outvoice.user.workspace.team;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
//
//    List<TeamEntity> findAllByWorkspaceIdAndDeletedAtIsNull(UUID workspaceId);
//
//    Optional<TeamEntity> findByIdAndWorkspaceIdAndDeletedAtIsNull(UUID id, UUID workspaceId);
//
//    boolean existsByNameIgnoreCaseAndWorkspaceIdAndDeletedAtIsNull(String name, UUID workspaceId);
//    boolean existsByNameIgnoreCaseAndWorkspaceIdAndIdNotAndDeletedAtIsNull(String name, UUID workspaceId, UUID excludeId);
//}
