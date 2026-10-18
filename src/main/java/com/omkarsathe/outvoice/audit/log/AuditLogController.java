package com.omkarsathe.outvoice.audit.log;

import com.omkarsathe.outvoice.audit.log.dto.AuditLogResponse;
import com.omkarsathe.outvoice.audit.log.dto.AuditLogStatsResponse;
import com.omkarsathe.outvoice.common.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public PagedResponse<AuditLogResponse> getAuditLogs(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return auditService.getAuditLogs(workspaceId, resourceType, action, search, from, to, page, size);
    }

    @GetMapping("/stats")
    public AuditLogStatsResponse getStats(@PathVariable UUID workspaceId) {
        return auditService.getStats(workspaceId);
    }
}
