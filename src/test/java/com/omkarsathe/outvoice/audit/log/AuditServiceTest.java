package com.omkarsathe.outvoice.audit.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omkarsathe.outvoice.audit.log.dto.AuditLogResponse;
import com.omkarsathe.outvoice.audit.log.dto.AuditLogStatsResponse;
import com.omkarsathe.outvoice.common.dto.PagedResponse;
import com.omkarsathe.outvoice.workspace.user.User;
import com.omkarsathe.outvoice.workspace.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldLogAuditRecordWithAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .fullName("John Doe")
                .email("john@example.com")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Map<String, Object> changes = Map.of(
                "invoiceNumber", "INV-0001",
                "amount", 1000.0,
                "keySecret", "super-secret-key"
        );

        auditService.log(
                workspaceId,
                AuditAction.CREATE,
                AuditResourceType.INVOICE,
                resourceId,
                "Created invoice INV-0001",
                changes
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(saved.getAction()).isEqualTo("CREATE");
        assertThat(saved.getResourceType()).isEqualTo("INVOICE");
        assertThat(saved.getResourceId()).isEqualTo(resourceId);
        assertThat(saved.getChangedBy()).isEqualTo(userId);
        assertThat(saved.getActorName()).isEqualTo("John Doe");
        assertThat(saved.getActorEmail()).isEqualTo("john@example.com");
        assertThat(saved.getChangeDescription()).isEqualTo("Created invoice INV-0001");
        assertThat(saved.getChangedAt()).isNotNull();

        // Sensitive key masking check
        JsonNode node = saved.getChanges();
        assertThat(node.get("invoiceNumber").asText()).isEqualTo("INV-0001");
        assertThat(node.get("amount").asDouble()).isEqualTo(1000.0);
        assertThat(node.get("keySecret").asText()).isEqualTo("***");
    }

    @Test
    void shouldFallbackToSystemWhenUnauthenticated() {
        UUID workspaceId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        auditService.log(
                workspaceId,
                AuditAction.PAYMENT_RECEIVED,
                AuditResourceType.INVOICE,
                resourceId,
                "Payment received via webhook",
                Map.of("amount", 500.0),
                "Razorpay Webhook",
                "webhook@razorpay.com",
                null
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(saved.getActorName()).isEqualTo("Razorpay Webhook");
        assertThat(saved.getActorEmail()).isEqualTo("webhook@razorpay.com");
        assertThat(saved.getChangedBy()).isNull();
    }

    @Test
    void shouldRetrievePaginatedAuditLogs() {
        UUID workspaceId = UUID.randomUUID();
        AuditLog log1 = AuditLog.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .action("CREATE")
                .resourceType("CUSTOMER")
                .resourceId(UUID.randomUUID())
                .actorName("Alice")
                .actorEmail("alice@example.com")
                .changeDescription("Created customer Alice")
                .changes(objectMapper.createObjectNode())
                .changedAt(Instant.now())
                .build();

        Page<AuditLog> page = new PageImpl<>(List.of(log1));
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PagedResponse<AuditLogResponse> response = auditService.getAuditLogs(
                workspaceId, null, null, null, null, null, 0, 20
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).changeDescription()).isEqualTo("Created customer Alice");
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnAggregatedStats() {
        UUID workspaceId = UUID.randomUUID();
        when(auditLogRepository.countByWorkspaceId(workspaceId)).thenReturn(42L);
        when(auditLogRepository.countByWorkspaceIdAndChangedAtAfter(eq(workspaceId), any(Instant.class))).thenReturn(5L);

        List<Object[]> resourceCounts = new ArrayList<>();
        resourceCounts.add(new Object[]{"INVOICE", 30L});
        resourceCounts.add(new Object[]{"CUSTOMER", 12L});
        when(auditLogRepository.countByResourceTypeGrouped(workspaceId)).thenReturn(resourceCounts);

        List<Object[]> actionCounts = new ArrayList<>();
        actionCounts.add(new Object[]{"CREATE", 25L});
        actionCounts.add(new Object[]{"UPDATE", 17L});
        when(auditLogRepository.countByActionGrouped(workspaceId)).thenReturn(actionCounts);

        AuditLogStatsResponse stats = auditService.getStats(workspaceId);

        assertThat(stats.totalEvents()).isEqualTo(42L);
        assertThat(stats.eventsToday()).isEqualTo(5L);
        assertThat(stats.eventsByResource()).containsEntry("INVOICE", 30L);
        assertThat(stats.eventsByAction()).containsEntry("CREATE", 25L);
    }
}
