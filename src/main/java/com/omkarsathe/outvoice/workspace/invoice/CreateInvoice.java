package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.workspace.invoice.item.CreateItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateInvoice(
        @NotNull(message = "Customer must be present")
        UUID customerId,

        @NotNull(message = "Issue date must be present")
        LocalDate issueDate,

        @NotNull(message = "Due date must be present")
        LocalDate dueDate,

//        @NotNull(message = "Total cannot be null")
//        @Digits(integer = 6, fraction = 2, message = "Total format must be up to 6 integer digits and 2 decimals")
//        @DecimalMin(value = "0.01", message = "Total must be at least 0.01")
//        @DecimalMax(value = "999999.99", message = "Total cannot exceed 999999.99")
//        BigDecimal total,

        @NotNull(message = "Tax cannot be null")
        @PositiveOrZero(message = "Tax must be positive or zero")
        BigDecimal tax,

        @NotNull(message = "Discount cannot be null")
        @PositiveOrZero(message = "Discount must be positive or zero")
        BigDecimal discount,

        @NotEmpty(message = "At least one item is required")
        @Valid
        List<CreateItem> items
) {
}
