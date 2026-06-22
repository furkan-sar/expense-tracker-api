package com.expensetracker.api.exception;

import com.expensetracker.api.dto.ErrorResponse;
import com.expensetracker.api.dto.FieldErrorResponse;
import com.expensetracker.api.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException exception) {
        log.warn(
                "requestId={} errorCode={} exceptionClass={} entityId={} currentUserId={} message={}",
                requestId(),
                "VALIDATION_ERROR",
                exception.getClass().getName(),
                null,
                authenticatedUser(),
                exception.getMessage(),
                exception
        );

        List<FieldErrorResponse> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();

        return new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Request validation failed.",
                details
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        log.warn(
                "requestId={} errorCode={} exceptionClass={} entityId={} currentUserId={} message={}",
                requestId(),
                exception.getErrorCode(),
                exception.getClass().getName(),
                exception.getEntityId(),
                authenticatedUser(),
                exception.getMessage(),
                exception
        );

        return ResponseEntity.status(exception.getStatus()).body(new ErrorResponse(
                OffsetDateTime.now(),
                exception.getStatus().value(),
                exception.getErrorCode(),
                exception.getMessage(),
                null
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException exception) {
        log.warn(
                "requestId={} errorCode={} exceptionClass={} entityId={} currentUserId={} message={}",
                requestId(),
                "ACCESS_DENIED",
                exception.getClass().getName(),
                null,
                authenticatedUser(),
                exception.getMessage(),
                exception
        );

        return new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                "Access denied.",
                null
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedException(Exception exception) {
        log.error(
                "requestId={} errorCode={} exceptionClass={} entityId={} currentUserId={} message={}",
                requestId(),
                "INTERNAL_SERVER_ERROR",
                exception.getClass().getName(),
                null,
                authenticatedUser(),
                exception.getMessage(),
                exception
        );

        return new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Unexpected server error.",
                null
        );
    }

    private String requestId() {
        return MDC.get("requestId");
    }

    private String authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser.user().getId() == null ? null : authenticatedUser.user().getId().toString();
        }
        return null;
    }
}
