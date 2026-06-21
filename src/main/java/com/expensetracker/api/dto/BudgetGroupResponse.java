package com.expensetracker.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BudgetGroupResponse(
        UUID id,
        String name,
        String description,
        String currency,
        UUID ownerUserId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
