package com.omkarsathe.outvoice.platform.dto;

import com.omkarsathe.outvoice.organization.OrgStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PlatformOrgResponse {
    private UUID id;
    private String name;
    private String slug;
    private UUID ownerUserId;
    private String ownerEmail;
    private OrgStatus status;
    private long memberCount;
    private LocalDateTime createdAt;
}
