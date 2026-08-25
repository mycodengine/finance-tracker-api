package com.financetracker.integration.repository;

import com.financetracker.AbstractIntegrationTest;
import com.financetracker.domain.entity.Account;
import com.financetracker.domain.entity.Transaction;
import com.financetracker.domain.entity.User;
import com.financetracker.domain.enums.AccountType;
import com.financetracker.domain.enums.TransactionType;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionRepository integration tests")
@Transactional
class TransactionRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .name("Repo User")
                .email("repo@test.com")
                .password(new BCryptPasswordEncoder().encode("password"))
                .build());

        account = accountRepository.save(Account.builder()
                .user(user)
                .name("Test Account")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .build());
    }

    @Test
    @DisplayName("findByFilters returns only transactions for the given user")
    void findByFilters_onlyReturnsUserTransactions() {
        saveTransaction(TransactionType.INCOME, BigDecimal.valueOf(100), LocalDate.now());
        saveTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(50), LocalDate.now());

        Page<Transaction> page = transactionRepository.findByFilters(
                user.getId(), null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByFilters respects type filter")
    void findByFilters_respectsTypeFilter() {
        saveTransaction(TransactionType.INCOME, BigDecimal.valueOf(200), LocalDate.now());
        saveTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(80), LocalDate.now());

        Page<Transaction> page = transactionRepository.findByFilters(
                user.getId(), null, null, TransactionType.INCOME, null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getType()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    @DisplayName("findByFilters respects date range filter")
    void findByFilters_respectsDateRange() {
        LocalDate today = LocalDate.now();
        saveTransaction(TransactionType.INCOME, BigDecimal.valueOf(100), today.minusDays(10));
        saveTransaction(TransactionType.INCOME, BigDecimal.valueOf(200), today);

        Page<Transaction> page = transactionRepository.findByFilters(
                user.getId(), null, null, null, today.minusDays(1), today, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getAmount()).isEqualByComparingTo("200");
    }

    @Test
    @DisplayName("findMonthlyTotals aggregates by type for the given month")
    void findMonthlyTotals_aggregatesByType() {
        LocalDate date = LocalDate.now();
        saveTransaction(TransactionType.INCOME, BigDecimal.valueOf(1000), date);
        saveTransaction(TransactionType.INCOME, BigDecimal.valueOf(500), date);
        saveTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(300), date);

        List<Object[]> rows = transactionRepository.findMonthlyTotals(
                user.getId(), date.getYear(), date.getMonthValue());

        assertThat(rows).hasSize(2);
        // Find income row
        Object[] incomeRow = rows.stream()
                .filter(r -> r[0] == TransactionType.INCOME).findFirst().orElseThrow();
        assertThat((BigDecimal) incomeRow[1]).isEqualByComparingTo("1500");
    }

    private void saveTransaction(TransactionType type, BigDecimal amount, LocalDate date) {
        transactionRepository.save(Transaction.builder()
                .account(account)
                .type(type)
                .amount(amount)
                .date(date)
                .build());
    }
}
