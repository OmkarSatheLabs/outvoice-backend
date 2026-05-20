package com.omkarsathe.outvoice.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgInvitationRepository extends JpaRepository<OrgInvitation, UUID> {

    Optional<OrgInvitation> findByToken(String token);

    List<OrgInvitation> findByOrgIdAndApprovalStatusAndStatus(
            UUID orgId, InvitationApprovalStatus approvalStatus, InvitationStatus status);
}
