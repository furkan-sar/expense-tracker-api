package com.expensetracker.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionCreateRequest(
        @NotNull
        UUID budgetGroupId,

        @NotNull
        UUID categoryId,

        @NotNull
        TransactionType type,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @NotNull
        LocalDate transactionDate,

        @Size(max = 500)
        String note
) {
}
