package com.omkarsathe.outvoice.common.exception;

public class InvoicePdfGenerationException extends RuntimeException {
    public InvoicePdfGenerationException(String message) {
        super(message);
    }

    public InvoicePdfGenerationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
