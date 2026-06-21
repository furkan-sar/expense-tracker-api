package com.expensetracker.api.dto;

public record FieldErrorResponse(
        String field,
        String message
) {
}
