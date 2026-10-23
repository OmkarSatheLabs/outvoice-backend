package com.omkarsathe.outvoice.admin;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
import com.omkarsathe.outvoice.auth.AuthService;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.WorkspaceStatus;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import com.omkarsathe.outvoice.workspace.member.Member;
import com.omkarsathe.outvoice.workspace.member.MemberRepository;
import com.omkarsathe.outvoice.workspace.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private AdminWorkspaceResponseMapper adminWorkspaceResponseMapper;
    @Mock
    private AdminWorkspaceMemberResponseMapper adminWorkspaceMemberResponseMapper;
    @Mock
    private AuthService authService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminService adminService;

    private UUID workspaceId;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        workspace = Workspace.builder()
                .id(workspaceId)
                .name("Acme Corp")
                .invoiceNumberPrefix("ACM")
                .status(WorkspaceStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void getWorkspaces_returnsNonDeletedWorkspacesWithMetrics() {
        when(memberRepository.countMembersByWorkspace()).thenReturn(List.of());
        when(invoiceRepository.countInvoicesByWorkspace()).thenReturn(List.of());
        when(invoiceRepository.sumBilledByWorkspace()).thenReturn(List.of());
        when(workspaceRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()).thenReturn(List.of(workspace));

        AdminWorkspaceResponse expectedResponse = new AdminWorkspaceResponse(
                workspaceId, "Acme Corp", "ACM", WorkspaceStatus.ACTIVE, false, workspace.getCreatedAt(),
                new WorkspaceUsageMetrics(0, 0, BigDecimal.ZERO)
        );
        when(adminWorkspaceResponseMapper.toResponse(eq(workspace), any(WorkspaceUsageMetrics.class)))
                .thenReturn(expectedResponse);

        List<AdminWorkspaceResponse> responses = adminService.getWorkspaces();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("Acme Corp");
        assertThat(responses.get(0).suspended()).isFalse();
    }

    @Test
    void getWorkspaceMembers_returnsMembersWithContactDetails() {
        when(workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)).thenReturn(Optional.of(workspace));

        User userEmailOnly = User.builder().id(UUID.randomUUID()).fullName("Alice").email("alice@example.com").build();
        User userMobileOnly = User.builder().id(UUID.randomUUID()).fullName("Bob")
                .phoneCode(PhoneCode.builder().code("+91").build())
                .mobile("9876543210").build();

        Member m1 = Member.builder().id(UUID.randomUUID()).workspace(workspace).user(userEmailOnly).build();
        Member m2 = Member.builder().id(UUID.randomUUID()).workspace(workspace).user(userMobileOnly).build();

        when(memberRepository.findAllByWorkspaceId(workspaceId)).thenReturn(List.of(m1, m2));

        AdminWorkspaceMemberResponse r1 = new AdminWorkspaceMemberResponse(userEmailOnly.getId(), "Alice", "alice@example.com", null, null);
        AdminWorkspaceMemberResponse r2 = new AdminWorkspaceMemberResponse(userMobileOnly.getId(), "Bob", null, "+91", "9876543210");

        when(adminWorkspaceMemberResponseMapper.toResponse(m1)).thenReturn(r1);
        when(adminWorkspaceMemberResponseMapper.toResponse(m2)).thenReturn(r2);

        List<AdminWorkspaceMemberResponse> result = adminService.getWorkspaceMembers(workspaceId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).email()).isEqualTo("alice@example.com");
        assertThat(result.get(1).mobile()).isEqualTo("9876543210");
        assertThat(result.get(1).phoneCode()).isEqualTo("+91");
    }

    @Test
    void getWorkspaceMembers_throwsNotFound_whenWorkspaceDoesNotExist() {
        when(workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getWorkspaceMembers(workspaceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workspace not found");
    }

    @Test
    void updateWorkspace_updatesFieldsAndRecordsAuditLog() {
        when(workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUpdateWorkspaceRequest request = new AdminUpdateWorkspaceRequest("Acme Global", "AGL");

        when(adminWorkspaceResponseMapper.toResponse(eq(workspace), any(WorkspaceUsageMetrics.class)))
                .thenReturn(new AdminWorkspaceResponse(workspaceId, "Acme Global", "AGL", WorkspaceStatus.ACTIVE, false, workspace.getCreatedAt(),
                        new WorkspaceUsageMetrics(0, 0, BigDecimal.ZERO)));

        AdminWorkspaceResponse response = adminService.updateWorkspace(workspaceId, request);

        assertThat(workspace.getName()).isEqualTo("Acme Global");
        assertThat(workspace.getInvoiceNumberPrefix()).isEqualTo("AGL");

        verify(auditService).log(
                eq(workspaceId),
                eq(AuditAction.UPDATE),
                eq(AuditResourceType.WORKSPACE),
                eq(workspaceId),
                contains("Acme Global"),
                any(Map.class)
        );
    }

    @Test
    void toggleSuspend_suspendsActiveWorkspaceAndAudits() {
        when(workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean suspended = adminService.toggleSuspend(workspaceId);

        assertThat(suspended).isTrue();
        assertThat(workspace.getStatus()).isEqualTo(WorkspaceStatus.SUSPENDED);

        verify(auditService).log(
                eq(workspaceId),
                eq(AuditAction.STATUS_CHANGE),
                eq(AuditResourceType.WORKSPACE),
                eq(workspaceId),
                contains("Suspended"),
                any(Map.class)
        );
    }

    @Test
    void toggleSuspend_reactivatesSuspendedWorkspace() {
        workspace.setStatus(WorkspaceStatus.SUSPENDED);
        when(workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean suspended = adminService.toggleSuspend(workspaceId);

        assertThat(suspended).isFalse();
        assertThat(workspace.getStatus()).isEqualTo(WorkspaceStatus.ACTIVE);

        verify(auditService).log(
                eq(workspaceId),
                eq(AuditAction.STATUS_CHANGE),
                eq(AuditResourceType.WORKSPACE),
                eq(workspaceId),
                contains("Reactivated"),
                any(Map.class)
        );
    }

    @Test
    void deleteWorkspace_softDeletesWorkspaceAndAudits() {
        when(workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));

        adminService.deleteWorkspace(workspaceId);

        assertThat(workspace.getDeletedAt()).isNotNull();
        assertThat(workspace.getStatus()).isEqualTo(WorkspaceStatus.INACTIVE);

        verify(auditService).log(
                eq(workspaceId),
                eq(AuditAction.DELETE),
                eq(AuditResourceType.WORKSPACE),
                eq(workspaceId),
                contains("Soft deleted"),
                any(Map.class)
        );
    }
}
