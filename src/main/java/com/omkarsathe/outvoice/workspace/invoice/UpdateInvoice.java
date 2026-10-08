package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.workspace.invoice.item.CreateItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateInvoice(
        @NotNull(message = "Customer must be present")
        UUID customerId,

        @NotNull(message = "Issue date must be present")
        LocalDate issueDate,

        @NotNull(message = "Due date must be present")
        LocalDate dueDate,

        @NotNull(message = "Tax cannot be null")
        @PositiveOrZero(message = "Tax must be positive or zero")
        BigDecimal tax,

        @NotNull(message = "Discount cannot be null")
        @PositiveOrZero(message = "Discount must be positive or zero")
        BigDecimal discount,

        @NotEmpty(message = "At least one item is required")
        @Valid
        List<CreateItem> items,

        boolean isDraft
) {
}
