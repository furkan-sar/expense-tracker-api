package com.expensetracker.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryReportItem(
        UUID categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal totalAmount,
        long transactionCount
) {
}
