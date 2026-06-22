package com.expensetracker.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryReportItem(
        UUID categoryId,
        String categoryName,
        TransactionType transactionType,
        BigDecimal totalAmount,
        BigDecimal percentage,
        String currency
) {
}
