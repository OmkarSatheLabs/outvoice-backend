package com.omkarsathe.outvoice.admin;

import com.omkarsathe.outvoice.auth.AuthService;
import com.omkarsathe.outvoice.auth.SignupRequest;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.*;
import com.omkarsathe.outvoice.workspace.dto.AdminWorkspaceDto;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import com.omkarsathe.outvoice.workspace.member.Member;
import com.omkarsathe.outvoice.workspace.member.MemberRepository;
import com.omkarsathe.outvoice.workspace.user.CreateUser;
import com.omkarsathe.outvoice.workspace.user.User;
import com.omkarsathe.outvoice.workspace.user.UserResponse;
import com.omkarsathe.outvoice.workspace.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.net.ssl.SSLSession;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;
    private final InvoiceRepository invoiceRepository;
    private final AdminWorkspaceResponseMapper adminWorkspaceResponseMapper;
    private final AdminWorkspaceMemberResponseMapper adminWorkspaceMemberResponseMapper;
    private final AuthService authService;

    public List<AdminWorkspaceResponse> getWorkspaces() {

        Map<UUID, Integer> memberCounts =
                memberRepository.countMembersByWorkspace()
                        .stream()
                        .collect(Collectors.toMap(
                                WorkspaceCountProjection::getWorkspaceId,
                                p -> Math.toIntExact(p.getTotal())
                        ));

        Map<UUID, Integer> invoiceCounts =
                invoiceRepository.countInvoicesByWorkspace()
                        .stream()
                        .collect(Collectors.toMap(
                                WorkspaceCountProjection::getWorkspaceId,
                                p -> Math.toIntExact(p.getTotal())
                        ));

        Map<UUID, BigDecimal> billedAmounts =
                invoiceRepository.sumBilledByWorkspace()
                        .stream()
                        .collect(Collectors.toMap(
                                WorkspaceBilledProjection::getWorkspaceId,
                                WorkspaceBilledProjection::getTotal
                        ));

        return workspaceRepository.findAll()
                .stream()
                .map(workspace -> {
                    UUID id = workspace.getId();

                    WorkspaceUsageMetrics metrics =
                            new WorkspaceUsageMetrics(
                                    memberCounts.getOrDefault(id, 0),
                                    invoiceCounts.getOrDefault(id, 0),
                                    billedAmounts.getOrDefault(
                                            id, BigDecimal.ZERO
                                    )
                            );

                    return adminWorkspaceResponseMapper.toResponse(workspace, metrics);
                })
                .toList();
    }

    public List<AdminWorkspaceMemberResponse> getWorkspaceMembers(UUID workspaceId) {
        return memberRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(adminWorkspaceMemberResponseMapper::toResponse)
                .toList();
    }

    public AdminWorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        SignupRequest signupRequest = new SignupRequest(
                new CreateUser(
                        request.ownerEmail(),
                        null,
                        null,
                        request.ownerName(),
                        "Pass@123"
                ),
                new CreateWorkspace(request.name())
        );

        authService.signup(signupRequest);

        Member ownerMembership = memberRepository
                .findByOwnerEmailAndWorkspaceName(
                        request.ownerEmail(),
                        request.name()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Created workspace membership not found"
                ));

        Workspace workspace = ownerMembership.getWorkspace();
        User owner = ownerMembership.getUser();

        WorkspaceUsageMetrics metrics = new WorkspaceUsageMetrics(0, 0, BigDecimal.ZERO);

        return adminWorkspaceResponseMapper.toResponse(workspace, metrics);
    }
}
