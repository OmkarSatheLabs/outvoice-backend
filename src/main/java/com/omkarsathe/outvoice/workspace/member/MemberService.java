package com.omkarsathe.outvoice.workspace.member;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.WorkspaceService;
import com.omkarsathe.outvoice.workspace.user.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MemberMapper memberMapper;

    public MemberResponse create(UUID workspaceId, UUID userId) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workspace not found: " + workspaceId
                        ));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + userId
                        ));

        Member member = Member.builder()
                .workspace(workspace)
                .user(user)
                .build();

        return memberMapper.toResponse(
                memberRepository.save(member)
        );
    }

    public List<MemberResponse> findByWorkspaceId(UUID workspaceId) {
        return memberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(member -> new MemberResponse(
                        member.getId(),
                        userMapper.toResponse(member.getUser())
                ))
                .toList();
    }
}
