package com.omkarsathe.outvoice.audit.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.omkarsathe.outvoice.audit.log.dto.AuditLogResponse;
import com.omkarsathe.outvoice.audit.log.dto.AuditLogStatsResponse;
import com.omkarsathe.outvoice.common.dto.PagedResponse;
import com.omkarsathe.outvoice.workspace.user.User;
import com.omkarsathe.outvoice.workspace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final Pattern SENSITIVE_KEY_PATTERN =
            Pattern.compile("(?i).*(secret|password|token|credential).*");

    /**
     * Primary logging method for workspace operations.
     */
    public void log(
            UUID workspaceId,
            AuditAction action,
            AuditResourceType resourceType,
            UUID resourceId,
            String changeDescription,
            Object changes
    ) {
        ActorInfo actor = resolveCurrentActor();
        log(workspaceId, action, resourceType, resourceId, changeDescription, changes,
                actor.name(), actor.email(), actor.userId());
    }

    /**
     * Overloaded logging method with explicit actor attribution (e.g. for webhooks or public actions).
     */
    public void log(
            UUID workspaceId,
            AuditAction action,
            AuditResourceType resourceType,
            UUID resourceId,
            String changeDescription,
            Object changes,
            String actorName,
            String actorEmail,
            UUID changedBy
    ) {
        try {
            JsonNode sanitizedChanges = sanitizeAndConvertToJson(changes);

            AuditLog auditLog = AuditLog.builder()
                    .workspaceId(workspaceId)
                    .action(action != null ? action.name() : "UPDATE")
                    .resourceType(resourceType != null ? resourceType.name() : "GENERAL")
                    .resourceId(resourceId != null ? resourceId : UUID.randomUUID())
                    .changedBy(changedBy)
                    .actorName(actorName != null ? actorName : "System")
                    .actorEmail(actorEmail != null ? actorEmail : "system@outvoice.app")
                    .changeDescription(changeDescription != null ? changeDescription : "Workspace updated")
                    .changes(sanitizedChanges)
                    .changedAt(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to record audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Backward-compatible stub adapter.
     */
    public void log(String resourceType, UUID changedBy, UUID resourceId) {
        log(null, AuditAction.UPDATE, AuditResourceType.WORKSPACE, resourceId,
                "Resource updated: " + resourceType, null, "System", null, changedBy);
    }

    /**
     * Retrieves paginated audit logs with dynamic filtering.
     */
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogs(
            UUID workspaceId,
            String resourceType,
            String action,
            String search,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        int pageNumber = Math.max(0, page);
        int pageSize = Math.min(Math.max(1, size), 100);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "changedAt"));
        Specification<AuditLog> spec = AuditLogSpecification.filter(
                workspaceId, resourceType, action, search, from, to
        );

        Page<AuditLog> resultPage = auditLogRepository.findAll(spec, pageable);

        List<AuditLogResponse> dtoList = resultPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                dtoList,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
    }

    /**
     * Retrieves aggregated activity statistics for a workspace.
     */
    @Transactional(readOnly = true)
    public AuditLogStatsResponse getStats(UUID workspaceId) {
        long total = auditLogRepository.countByWorkspaceId(workspaceId);
        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long today = auditLogRepository.countByWorkspaceIdAndChangedAtAfter(workspaceId, startOfToday);

        Map<String, Long> byResource = new HashMap<>();
        List<Object[]> resourceRows = auditLogRepository.countByResourceTypeGrouped(workspaceId);
        for (Object[] row : resourceRows) {
            if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                byResource.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        Map<String, Long> byAction = new HashMap<>();
        List<Object[]> actionRows = auditLogRepository.countByActionGrouped(workspaceId);
        for (Object[] row : actionRows) {
            if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                byAction.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        return new AuditLogStatsResponse(total, today, byResource, byAction);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getWorkspaceId(),
                log.getAction(),
                log.getResourceType(),
                log.getResourceId(),
                log.getChangedBy(),
                log.getActorName(),
                log.getActorEmail(),
                log.getChangeDescription(),
                log.getChanges(),
                log.getChangedAt()
        );
    }

    private ActorInfo resolveCurrentActor() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();

                if (principal instanceof User user) {
                    return new ActorInfo(user.getId(), user.getFullName(), user.getEmail());
                }

                if (principal instanceof UserDetails userDetails) {
                    try {
                        UUID userId = UUID.fromString(userDetails.getUsername());
                        Optional<User> userOpt = userRepository.findById(userId);
                        if (userOpt.isPresent()) {
                            User u = userOpt.get();
                            return new ActorInfo(u.getId(), u.getFullName(), u.getEmail());
                        }
                        return new ActorInfo(userId, "Member (" + userId.toString().substring(0, 8) + ")", null);
                    } catch (Exception ignored) {
                        return new ActorInfo(null, userDetails.getUsername(), null);
                    }
                }
            }
        } catch (Exception ignored) {}

        return new ActorInfo(null, "System", "system@outvoice.app");
    }

    private JsonNode sanitizeAndConvertToJson(Object changes) {
        if (changes == null) {
            return objectMapper.createObjectNode();
        }

        JsonNode node;
        if (changes instanceof JsonNode jn) {
            node = jn.deepCopy();
        } else {
            node = objectMapper.valueToTree(changes);
        }

        if (node != null && node.isObject()) {
            maskSensitiveFields((ObjectNode) node);
        }

        return node != null ? node : objectMapper.createObjectNode();
    }

    private void maskSensitiveFields(ObjectNode objectNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (SENSITIVE_KEY_PATTERN.matcher(key).matches()) {
                if (value.isTextual() && !value.asText().isEmpty()) {
                    entry.setValue(objectNode.textNode("***"));
                }
            } else if (value.isObject()) {
                maskSensitiveFields((ObjectNode) value);
            }
        }
    }

    private record ActorInfo(UUID userId, String name, String email) {}
}
