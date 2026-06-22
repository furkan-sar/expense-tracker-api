package com.expensetracker.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SummaryReportResponse(
        UUID budgetGroupId,
        String budgetGroupName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        String currency
) {
}
