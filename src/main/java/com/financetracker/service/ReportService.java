package com.financetracker.service;

import com.financetracker.domain.entity.Account;
import com.financetracker.domain.entity.User;
import com.financetracker.domain.enums.TransactionType;
import com.financetracker.dto.response.BalanceHistoryResponse;
import com.financetracker.dto.response.CategoryBreakdownResponse;
import com.financetracker.dto.response.MonthlySummaryResponse;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /** Returns total income, expenses, and net balance for the given year and month. */
    public MonthlySummaryResponse getMonthlySummary(User user, int year, int month) {
        List<Object[]> rows = transactionRepository.findMonthlyTotals(user.getId(), year, month);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Object[] row : rows) {
            TransactionType type = (TransactionType) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            if (type == TransactionType.INCOME) {
                totalIncome = amount;
            } else if (type == TransactionType.EXPENSE) {
                totalExpense = amount;
            }
        }

        log.debug("Monthly summary for user {} — {}/{}: income={}, expense={}",
                user.getId(), year, month, totalIncome, totalExpense);

        return new MonthlySummaryResponse(year, month, totalIncome, totalExpense,
                totalIncome.subtract(totalExpense));
    }

    /** Returns expense totals per category with percentage share of total spending. */
    public List<CategoryBreakdownResponse> getCategoryBreakdown(
            User user, LocalDate startDate, LocalDate endDate) {

        List<Object[]> rows = transactionRepository.findExpenseByCategory(
                user.getId(), startDate, endDate);

        BigDecimal grandTotal = rows.stream()
                .map(r -> (BigDecimal) r[3])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(row -> {
            BigDecimal amount = (BigDecimal) row[3];
            double pct = grandTotal.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : amount.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();

            return new CategoryBreakdownResponse(
                    (Long) row[0],   // categoryId
                    (String) row[1], // categoryName
                    (String) row[2], // categoryColor
                    amount,
                    (Long) row[4],   // transactionCount
                    pct);
        }).toList();
    }

    /**
     * Returns the running balance for an account over the given date range.
     * Starts from the current account balance and works backwards using daily net changes.
     */
    public List<BalanceHistoryResponse> getBalanceHistory(
            User user, Long accountId, LocalDate startDate, LocalDate endDate) {

        Account account = accountRepository.findByIdAndUser(accountId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        List<Object[]> dailyChanges = transactionRepository.findDailyNetChanges(
                user.getId(), accountId, startDate, endDate);

        // Compute running balance from the first date in range
        List<BalanceHistoryResponse> history = new ArrayList<>();
        BigDecimal runningBalance = account.getBalance();

        // Subtract changes after endDate to back-calculate the starting balance
        BigDecimal totalChangeInRange = dailyChanges.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        runningBalance = runningBalance.subtract(totalChangeInRange);

        for (Object[] row : dailyChanges) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal netChange = (BigDecimal) row[1];
            runningBalance = runningBalance.add(netChange);
            history.add(new BalanceHistoryResponse(date, runningBalance));
        }

        return history;
    }
}
