package com.omkarsathe.outvoice.organization.dto;

import com.omkarsathe.outvoice.organization.OrgStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OrgResponse {
    private UUID id;
    private String name;
    private String slug;
    private UUID ownerUserId;
    private OrgStatus status;
    private LocalDateTime createdAt;
}
