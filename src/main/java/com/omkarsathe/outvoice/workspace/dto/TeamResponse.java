package com.omkarsathe.outvoice.workspace.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    private UUID id;
    private String name;
    private String description;
}
