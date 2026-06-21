package com.expensetracker.api.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SummaryReportRow {

    LocalDate getStartDate();

    LocalDate getEndDate();

    BigDecimal getTotalIncome();

    BigDecimal getTotalExpense();
}
