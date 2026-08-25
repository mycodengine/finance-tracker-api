package com.financetracker.dto.response;

import com.financetracker.domain.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String name,
        AccountType type,
        BigDecimal balance,
        String currency,
        LocalDateTime createdAt
) {
}
