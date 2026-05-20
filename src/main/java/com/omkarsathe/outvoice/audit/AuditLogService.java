package com.omkarsathe.outvoice.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omkarsathe.outvoice.organization.Organization;
import com.omkarsathe.outvoice.organization.OrganizationRepository;
import com.omkarsathe.outvoice.user.User;
import com.omkarsathe.outvoice.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Writes immutable audit records for all significant role/permission events.
 * Runs in a separate transaction so audit logs are committed even if the calling transaction rolls back.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UUID orgId,
                    UUID actorUserId,
                    AuditAction action,
                    UUID targetUserId,
                    Object oldValue,
                    Object newValue,
                    HttpServletRequest request) {
        try {
            Organization org = orgId != null
                    ? organizationRepository.findById(orgId).orElse(null)
                    : null;
            User actor = userRepository.findById(actorUserId).orElse(null);
            User target = targetUserId != null
                    ? userRepository.findById(targetUserId).orElse(null)
                    : null;

            String ip = resolveIp(request);
            String oldJson = serialize(oldValue);
            String newJson = serialize(newValue);

            AuditLog entry = AuditLog.builder()
                    .org(org)
                    .actor(actor)
                    .action(action)
                    .target(target)
                    .oldValue(oldJson)
                    .newValue(newJson)
                    .ipAddress(ip)
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log for action={} actor={}: {}", action, actorUserId, e.getMessage());
        }
    }

    private String serialize(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return value.toString();
        }
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
