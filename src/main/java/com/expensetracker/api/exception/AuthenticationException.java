package com.expensetracker.api.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {

    public AuthenticationException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", message);
    }

    public AuthenticationException(String message, String entityId) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", message, entityId);
    }
}
