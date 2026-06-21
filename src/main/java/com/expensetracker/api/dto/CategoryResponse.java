package com.expensetracker.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID budgetGroupId,
        String name,
        TransactionType type,
        String color,
        String icon,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
