package com.omkarsathe.outvoice.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a business rule is violated (e.g. invalid invite token, ownership transfer to non-super-user). */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
