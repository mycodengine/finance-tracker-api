package com.financetracker.dto.response;

import java.math.BigDecimal;

/** Monthly income vs expense summary for a given year/month. */
public record MonthlySummaryResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance
) {
}
