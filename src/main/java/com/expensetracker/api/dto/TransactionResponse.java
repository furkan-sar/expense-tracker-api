package com.expensetracker.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID budgetGroupId,
        CategoryResponse category,
        BudgetMemberResponse member,
        TransactionType type,
        BigDecimal amount,
        LocalDate transactionDate,
        String note,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
