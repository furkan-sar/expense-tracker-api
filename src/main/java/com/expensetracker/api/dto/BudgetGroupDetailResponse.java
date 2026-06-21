package com.expensetracker.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BudgetGroupDetailResponse(
        UUID id,
        String name,
        String description,
        String currency,
        UUID ownerUserId,
        List<BudgetMemberResponse> members,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
