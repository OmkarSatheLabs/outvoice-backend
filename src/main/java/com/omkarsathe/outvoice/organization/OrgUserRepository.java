package com.omkarsathe.outvoice.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgUserRepository extends JpaRepository<OrgUser, UUID> {

    Optional<OrgUser> findByOrgIdAndUserId(UUID orgId, UUID userId);

    List<OrgUser> findByUserIdAndStatus(UUID userId, OrgUserStatus status);

    List<OrgUser> findByOrgIdAndStatus(UUID orgId, OrgUserStatus status);

    List<OrgUser> findByOrgId(UUID orgId);

    boolean existsByOrgIdAndUserIdAndStatus(UUID orgId, UUID userId, OrgUserStatus status);

    @Query("SELECT ou FROM OrgUser ou WHERE ou.org.id = :orgId AND ou.role = :role AND ou.status = 'ACTIVE'")
    List<OrgUser> findActiveByOrgIdAndRole(@Param("orgId") UUID orgId, @Param("role") OrgRole role);

    @Query("SELECT ou FROM OrgUser ou WHERE ou.org.id = :orgId AND ou.user.id = :userId AND ou.status = 'ACTIVE'")
    Optional<OrgUser> findActiveByOrgIdAndUserId(@Param("orgId") UUID orgId, @Param("userId") UUID userId);
}
