package com.omkarsathe.outvoice.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TransferOwnershipRequest {
    @NotNull
    private UUID targetUserId;

    /** Current owner's password, required to confirm the irreversible transfer. */
    @NotBlank
    private String passwordConfirmation;
}
