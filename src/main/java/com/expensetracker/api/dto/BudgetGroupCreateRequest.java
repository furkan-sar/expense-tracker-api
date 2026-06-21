package com.expensetracker.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BudgetGroupCreateRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @Size(max = 500)
        String description,

        @NotBlank
        @Size(min = 3, max = 3)
        @Pattern(regexp = "^[A-Z]{3}$")
        String currency
) {
}
