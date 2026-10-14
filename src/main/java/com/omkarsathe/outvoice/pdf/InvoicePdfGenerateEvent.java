package com.omkarsathe.outvoice.pdf;

import java.util.UUID;

public record InvoicePdfGenerateEvent(
        UUID processorId
) {
}
