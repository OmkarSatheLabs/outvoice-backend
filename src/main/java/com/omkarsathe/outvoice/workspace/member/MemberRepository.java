package com.omkarsathe.outvoice.workspace.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    List<Member> findByWorkspaceId(UUID workspaceId);

    List<Member> findByUserId(UUID userId);
}
