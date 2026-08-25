package com.financetracker.unit.service;

import com.financetracker.domain.entity.User;
import com.financetracker.domain.enums.TransactionType;
import com.financetracker.dto.response.CategoryBreakdownResponse;
import com.financetracker.dto.response.MonthlySummaryResponse;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService unit tests")
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ReportService reportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").build();
    }

    @Test
    @DisplayName("getMonthlySummary calculates correct net balance")
    void getMonthlySummary_calculatesNetBalance() {
        given(transactionRepository.findMonthlyTotals(1L, 2024, 8)).willReturn(List.of(
                new Object[]{TransactionType.INCOME, BigDecimal.valueOf(5000)},
                new Object[]{TransactionType.EXPENSE, BigDecimal.valueOf(2000)}
        ));

        MonthlySummaryResponse summary = reportService.getMonthlySummary(user, 2024, 8);

        assertThat(summary.totalIncome()).isEqualByComparingTo("5000");
        assertThat(summary.totalExpense()).isEqualByComparingTo("2000");
        assertThat(summary.netBalance()).isEqualByComparingTo("3000");
    }

    @Test
    @DisplayName("getMonthlySummary returns zeros when no transactions exist")
    void getMonthlySummary_returnsZerosForEmptyMonth() {
        given(transactionRepository.findMonthlyTotals(1L, 2024, 1)).willReturn(List.of());

        MonthlySummaryResponse summary = reportService.getMonthlySummary(user, 2024, 1);

        assertThat(summary.totalIncome()).isEqualByComparingTo("0");
        assertThat(summary.totalExpense()).isEqualByComparingTo("0");
        assertThat(summary.netBalance()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("getCategoryBreakdown calculates correct percentages")
    void getCategoryBreakdown_calculatesPercentages() {
        given(transactionRepository.findExpenseByCategory(1L,
                LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 31))).willReturn(List.of(
                new Object[]{1L, "Food", "#EF4444", BigDecimal.valueOf(600), 10L},
                new Object[]{2L, "Transport", "#F97316", BigDecimal.valueOf(400), 5L}
        ));

        List<CategoryBreakdownResponse> result = reportService.getCategoryBreakdown(
                user, LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 31));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).percentage()).isCloseTo(60.0, within(0.01));
        assertThat(result.get(1).percentage()).isCloseTo(40.0, within(0.01));
    }

    private static <T extends Comparable<T>> org.assertj.core.data.Offset<T> within(T value) {
        return org.assertj.core.data.Offset.offset(value);
    }
}
