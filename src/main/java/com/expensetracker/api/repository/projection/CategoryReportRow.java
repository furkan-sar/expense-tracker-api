package com.expensetracker.api.repository.projection;

import com.expensetracker.api.entity.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public interface CategoryReportRow {

    UUID getCategoryId();

    String getCategoryName();

    TransactionType getType();

    BigDecimal getTotalAmount();

    long getTransactionCount();
}
