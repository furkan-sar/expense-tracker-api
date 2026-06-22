package com.expensetracker.api.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface MemberReportRow {

    UUID getBudgetGroupId();

    String getBudgetGroupName();

    UUID getMemberId();

    UUID getUserId();

    String getFirstName();

    String getLastName();

    BigDecimal getTotalIncome();

    BigDecimal getTotalExpense();

    long getTransactionCount();
}
