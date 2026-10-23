package com.omkarsathe.outvoice.workspace;

import com.omkarsathe.outvoice.common.exception.ForbiddenException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerMapping;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceAccessInterceptorTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private WorkspaceAccessInterceptor interceptor;

    private UUID workspaceId;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
    }

    @Test
    void preHandle_allowsAdminEndpointsBypass() {
        when(request.getRequestURI()).thenReturn("/api/admin/workspaces/" + workspaceId + "/members");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(workspaceRepository);
    }

    @Test
    void preHandle_allowsActiveWorkspace() {
        when(request.getRequestURI()).thenReturn("/api/workspaces/" + workspaceId + "/products");
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(Map.of("workspaceId", workspaceId.toString()));

        Workspace activeWorkspace = Workspace.builder()
                .id(workspaceId)
                .status(WorkspaceStatus.ACTIVE)
                .build();
        when(workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)).thenReturn(Optional.of(activeWorkspace));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_blocksSuspendedWorkspaceWithForbiddenException() {
        when(request.getRequestURI()).thenReturn("/api/workspaces/" + workspaceId + "/invoices");
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(Map.of("workspaceId", workspaceId.toString()));

        Workspace suspendedWorkspace = Workspace.builder()
                .id(workspaceId)
                .status(WorkspaceStatus.SUSPENDED)
                .build();
        when(workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)).thenReturn(Optional.of(suspendedWorkspace));

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Workspace is suspended");
    }

    @Test
    void preHandle_blocksSoftDeletedWorkspaceWithNotFoundException() {
        when(request.getRequestURI()).thenReturn("/api/workspaces/" + workspaceId + "/customers");
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(Map.of("workspaceId", workspaceId.toString()));

        when(workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workspace not found");
    }
}
