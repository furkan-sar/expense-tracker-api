package com.expensetracker.api.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", message);
    }

    public ConflictException(String message, String entityId) {
        super(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", message, entityId);
    }
}
