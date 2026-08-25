package com.financetracker.dto.request;

import com.financetracker.domain.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequest(

        @NotBlank(message = "Account name is required")
        @Size(max = 100, message = "Account name must not exceed 100 characters")
        String name,

        @NotNull(message = "Account type is required")
        AccountType type,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 10, message = "Currency code must be 3–10 characters")
        String currency,

        @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance cannot be negative")
        BigDecimal initialBalance
) {
}
