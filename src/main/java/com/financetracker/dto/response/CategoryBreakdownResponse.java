package com.financetracker.dto.response;

import java.math.BigDecimal;

/** Expense breakdown per category with a percentage share of total spending. */
public record CategoryBreakdownResponse(
        Long categoryId,
        String categoryName,
        String categoryColor,
        BigDecimal totalAmount,
        long transactionCount,
        double percentage
) {
}
