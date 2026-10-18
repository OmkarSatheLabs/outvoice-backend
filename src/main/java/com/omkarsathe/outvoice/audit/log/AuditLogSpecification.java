package com.omkarsathe.outvoice.audit.log;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuditLogSpecification {

    public static Specification<AuditLog> filter(
            UUID workspaceId,
            String resourceType,
            String action,
            String search,
            Instant from,
            Instant to
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("workspaceId"), workspaceId));

            if (StringUtils.hasText(resourceType)) {
                predicates.add(cb.equal(cb.upper(root.get("resourceType")), resourceType.trim().toUpperCase()));
            }

            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(cb.upper(root.get("action")), action.trim().toUpperCase()));
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate descMatch = cb.like(cb.lower(root.get("changeDescription")), pattern);
                Predicate nameMatch = cb.like(cb.lower(root.get("actorName")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("actorEmail")), pattern);
                predicates.add(cb.or(descMatch, nameMatch, emailMatch));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("changedAt"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("changedAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
