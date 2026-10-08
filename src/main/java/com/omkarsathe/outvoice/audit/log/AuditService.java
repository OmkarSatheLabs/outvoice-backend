package com.omkarsathe.outvoice.audit.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String resourceType, UUID changedBy, UUID resourceId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setResourceType(resourceType);
        auditLog.setChangedBy(changedBy);
        auditLog.setResourceId(resourceId);
        auditLogRepository.save(auditLog);
    }
}
