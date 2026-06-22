package com.expensetracker.api.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends ApiException {

    public AuthorizationException(String message) {
        super(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message);
    }

    public AuthorizationException(String message, String entityId) {
        super(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message, entityId);
    }
}
