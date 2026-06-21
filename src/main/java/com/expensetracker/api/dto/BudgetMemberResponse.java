package com.expensetracker.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BudgetMemberResponse(
        UUID id,
        UUID budgetGroupId,
        UserResponse user,
        BudgetMemberRole role,
        OffsetDateTime joinedAt
) {
}
