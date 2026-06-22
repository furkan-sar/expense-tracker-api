package com.expensetracker.api.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface CategoryReportRow {

    UUID getBudgetGroupId();

    String getBudgetGroupName();

    UUID getCategoryId();

    String getCategoryName();

    String getType();

    BigDecimal getTotalAmount();

    long getTransactionCount();
}
