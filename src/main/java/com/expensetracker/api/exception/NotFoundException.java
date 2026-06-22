package com.expensetracker.api.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }

    public NotFoundException(String message, String entityId) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message, entityId);
    }
}
