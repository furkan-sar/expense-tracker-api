package com.expensetracker.api.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final String entityId;

    protected ApiException(HttpStatus status, String errorCode, String message) {
        this(status, errorCode, message, null);
    }

    protected ApiException(HttpStatus status, String errorCode, String message, String entityId) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.entityId = entityId;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getEntityId() {
        return entityId;
    }
}
