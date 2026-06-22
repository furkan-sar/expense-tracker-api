package com.expensetracker.api.dto;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String errorCode,
        String message,
        List<FieldErrorResponse> details
) {
    public ErrorResponse {
        details = details == null ? Collections.emptyList() : List.copyOf(details);
    }
}
