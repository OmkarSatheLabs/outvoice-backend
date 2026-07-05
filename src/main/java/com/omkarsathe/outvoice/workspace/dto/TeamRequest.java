package com.omkarsathe.outvoice.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {
    @NotBlank(message = "Team name is required")
    private String name;
    private String description;
}
