package com.omkarsathe.outvoice.audit.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String resourceType, UUID changedBy, UUID resourceId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setResourceType(resourceType);
        auditLog.setChangeDescription("Someone has done something.");
        auditLog.setChanges(new ObjectMapper().createObjectNode());
        auditLog.setChangedBy(changedBy);
        auditLog.setResourceId(resourceId);
        auditLog.setChangedAt(Instant.now());
        auditLogRepository.save(auditLog);
    }
}
