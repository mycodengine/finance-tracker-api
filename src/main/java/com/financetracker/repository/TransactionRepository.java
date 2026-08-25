package com.financetracker.repository;

import com.financetracker.domain.entity.Transaction;
import com.financetracker.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Flexible filter query for transactions. Null parameters are ignored,
     * making all filters optional without requiring multiple derived methods.
     */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.account.user.id = :userId
              AND (:accountId  IS NULL OR t.account.id  = :accountId)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:type       IS NULL OR t.type         = :type)
              AND (:startDate  IS NULL OR t.date        >= :startDate)
              AND (:endDate    IS NULL OR t.date        <= :endDate)
            """)
    Page<Transaction> findByFilters(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("categoryId") Long categoryId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    Optional<Transaction> findByIdAndAccountUserId(Long id, Long userId);

    /** Totals grouped by transaction type for a given user/month — used by ReportService. */
    @Query("""
            SELECT t.type, SUM(t.amount)
            FROM Transaction t
            WHERE t.account.user.id = :userId
              AND YEAR(t.date)  = :year
              AND MONTH(t.date) = :month
            GROUP BY t.type
            """)
    List<Object[]> findMonthlyTotals(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month);

    /** Expense totals per category over a date range — used for the spending breakdown report. */
    @Query("""
            SELECT c.id, c.name, c.color, SUM(t.amount), COUNT(t)
            FROM Transaction t
            JOIN t.category c
            WHERE t.account.user.id = :userId
              AND t.type = 'EXPENSE'
              AND t.date BETWEEN :startDate AND :endDate
            GROUP BY c.id, c.name, c.color
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> findExpenseByCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** Daily net change (income − expense) for a specific account — used for balance history. */
    @Query("""
            SELECT t.date, SUM(
                CASE WHEN t.type = 'INCOME'   THEN  t.amount
                     WHEN t.type = 'EXPENSE'  THEN -t.amount
                     ELSE 0
                END)
            FROM Transaction t
            WHERE t.account.user.id = :userId
              AND t.account.id      = :accountId
              AND t.date BETWEEN :startDate AND :endDate
            GROUP BY t.date
            ORDER BY t.date
            """)
    List<Object[]> findDailyNetChanges(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
