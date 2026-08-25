package com.financetracker.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Running account balance on a specific date. */
public record BalanceHistoryResponse(
        LocalDate date,
        BigDecimal balance
) {
}
