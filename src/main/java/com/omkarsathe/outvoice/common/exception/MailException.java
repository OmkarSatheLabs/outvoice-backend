package com.omkarsathe.outvoice.common.exception;

public class MailException extends RuntimeException {
    public MailException(String message, Throwable cause) {
        super(message, cause);
    }
}
