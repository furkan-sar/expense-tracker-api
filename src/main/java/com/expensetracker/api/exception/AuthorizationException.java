package com.expensetracker.api.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends ApiException {

    public AuthorizationException(String message) {
        super(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message);
    }
}
