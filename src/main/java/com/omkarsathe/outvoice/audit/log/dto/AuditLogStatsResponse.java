package com.omkarsathe.outvoice.audit.log.dto;

import java.util.Map;

public record AuditLogStatsResponse(
        long totalEvents,
        long eventsToday,
        Map<String, Long> eventsByResource,
        Map<String, Long> eventsByAction
) {
}
