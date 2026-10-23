package com.omkarsathe.outvoice.workspace;

import com.omkarsathe.outvoice.common.exception.ForbiddenException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkspaceAccessInterceptor implements HandlerInterceptor {

    private final WorkspaceRepository workspaceRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();

        // Platform admin management endpoints are excluded from member cutoff checks
        if (uri.startsWith("/api/admin/")) {
            return true;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> uriVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriVariables != null && uriVariables.containsKey("workspaceId")) {
            String workspaceIdStr = uriVariables.get("workspaceId");
            try {
                UUID workspaceId = UUID.fromString(workspaceIdStr);
                Workspace workspace = workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

                if (workspace.isSuspended()) {
                    throw new ForbiddenException("Workspace is suspended. Access is prohibited.", workspaceId.toString());
                }
            } catch (IllegalArgumentException ignored) {
                // Not a valid UUID format; controller parameter validation will handle it
            }
        }

        return true;
    }
}
