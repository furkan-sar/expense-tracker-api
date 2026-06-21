package com.expensetracker.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MemberReportItem(
        UUID memberId,
        UUID userId,
        String firstName,
        String lastName,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        long transactionCount
) {
}
