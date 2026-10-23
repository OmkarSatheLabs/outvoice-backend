package com.omkarsathe.outvoice.admin;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
import com.omkarsathe.outvoice.auth.AuthService;
import com.omkarsathe.outvoice.auth.SignupRequest;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.*;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import com.omkarsathe.outvoice.workspace.member.Member;
import com.omkarsathe.outvoice.workspace.member.MemberRepository;
import com.omkarsathe.outvoice.workspace.user.CreateUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
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
    private final AuditService auditService;

    @Transactional(readOnly = true)
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

        return workspaceRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
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

    @Transactional(readOnly = true)
    public List<AdminWorkspaceMemberResponse> getWorkspaceMembers(UUID workspaceId) {
        workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        return memberRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(adminWorkspaceMemberResponseMapper::toResponse)
                .toList();
    }

    @Transactional
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
        WorkspaceUsageMetrics metrics = new WorkspaceUsageMetrics(1, 0, BigDecimal.ZERO);

        return adminWorkspaceResponseMapper.toResponse(workspace, metrics);
    }

    @Transactional
    public AdminWorkspaceResponse updateWorkspace(UUID workspaceId, AdminUpdateWorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        Map<String, Object> diff = new HashMap<>();
        String newName = request.name().trim();
        if (!Objects.equals(workspace.getName(), newName)) {
            diff.put("name", Map.of("old", workspace.getName(), "new", newName));
            workspace.setName(newName);
        }

        if (request.invoiceNumberPrefix() != null) {
            String newPrefix = request.invoiceNumberPrefix().trim();
            if (!Objects.equals(workspace.getInvoiceNumberPrefix(), newPrefix)) {
                diff.put("invoiceNumberPrefix", Map.of(
                        "old", workspace.getInvoiceNumberPrefix() != null ? workspace.getInvoiceNumberPrefix() : "",
                        "new", newPrefix
                ));
                workspace.setInvoiceNumberPrefix(newPrefix);
            }
        }

        Workspace updated = workspaceRepository.save(workspace);

        if (!diff.isEmpty()) {
            auditService.log(
                    workspaceId,
                    AuditAction.UPDATE,
                    AuditResourceType.WORKSPACE,
                    workspaceId,
                    "Updated workspace details for " + updated.getName(),
                    diff
            );
        }

        WorkspaceUsageMetrics metrics = getMetricsForWorkspace(workspaceId);
        return adminWorkspaceResponseMapper.toResponse(updated, metrics);
    }

    @Transactional
    public boolean toggleSuspend(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        WorkspaceStatus oldStatus = workspace.getStatus();
        WorkspaceStatus newStatus = (oldStatus == WorkspaceStatus.SUSPENDED)
                ? WorkspaceStatus.ACTIVE
                : WorkspaceStatus.SUSPENDED;

        workspace.setStatus(newStatus);
        workspaceRepository.save(workspace);

        boolean suspended = (newStatus == WorkspaceStatus.SUSPENDED);
        String actionDescription = suspended
                ? "Suspended workspace " + workspace.getName()
                : "Reactivated workspace " + workspace.getName();

        auditService.log(
                workspaceId,
                AuditAction.STATUS_CHANGE,
                AuditResourceType.WORKSPACE,
                workspaceId,
                actionDescription,
                Map.of("status", Map.of("old", oldStatus.name(), "new", newStatus.name()))
        );

        return suspended;
    }

    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        workspace.setDeletedAt(Instant.now());
        workspace.setStatus(WorkspaceStatus.INACTIVE);
        workspaceRepository.save(workspace);

        auditService.log(
                workspaceId,
                AuditAction.DELETE,
                AuditResourceType.WORKSPACE,
                workspaceId,
                "Soft deleted workspace " + workspace.getName(),
                Map.of("name", workspace.getName(), "deletedAt", workspace.getDeletedAt().toString())
        );
    }

    private WorkspaceUsageMetrics getMetricsForWorkspace(UUID workspaceId) {
        int members = memberRepository.findByWorkspaceId(workspaceId).size();
        return new WorkspaceUsageMetrics(members, 0, BigDecimal.ZERO);
    }
}
