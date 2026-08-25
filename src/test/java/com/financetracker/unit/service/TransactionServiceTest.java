package com.financetracker.unit.service;

import com.financetracker.domain.entity.Account;
import com.financetracker.domain.entity.Transaction;
import com.financetracker.domain.entity.User;
import com.financetracker.domain.enums.AccountType;
import com.financetracker.domain.enums.TransactionType;
import com.financetracker.dto.request.TransactionRequest;
import com.financetracker.dto.response.TransactionResponse;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.TransactionMapper;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService unit tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").build();
        account = Account.builder()
                .id(5L).user(user).name("Checking")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.valueOf(1000))
                .currency("USD")
                .build();
    }

    @Test
    @DisplayName("create INCOME transaction increases account balance")
    void create_incomeIncreasesBalance() {
        TransactionRequest request = new TransactionRequest(
                5L, null, TransactionType.INCOME, BigDecimal.valueOf(300), "Salary", LocalDate.now());

        given(accountRepository.findByIdAndUser(5L, user)).willReturn(Optional.of(account));
        given(transactionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(accountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(transactionMapper.toResponse(any())).willReturn(
                new TransactionResponse(1L, 5L, "Checking", null, null,
                        TransactionType.INCOME, BigDecimal.valueOf(300), "Salary", LocalDate.now(), null));

        transactionService.create(request, user);

        assertThat(account.getBalance()).isEqualByComparingTo("1300");
    }

    @Test
    @DisplayName("create EXPENSE transaction decreases account balance")
    void create_expenseDecreasesBalance() {
        TransactionRequest request = new TransactionRequest(
                5L, null, TransactionType.EXPENSE, BigDecimal.valueOf(200), "Groceries", LocalDate.now());

        given(accountRepository.findByIdAndUser(5L, user)).willReturn(Optional.of(account));
        given(transactionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(accountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(transactionMapper.toResponse(any())).willReturn(
                new TransactionResponse(1L, 5L, "Checking", null, null,
                        TransactionType.EXPENSE, BigDecimal.valueOf(200), "Groceries", LocalDate.now(), null));

        transactionService.create(request, user);

        assertThat(account.getBalance()).isEqualByComparingTo("800");
    }

    @Test
    @DisplayName("delete reverses balance on the account")
    void delete_reversesBalance() {
        Transaction existing = Transaction.builder()
                .id(20L).account(account).type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(150)).date(LocalDate.now()).build();

        given(transactionRepository.findByIdAndAccountUserId(20L, 1L)).willReturn(Optional.of(existing));
        given(accountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        transactionService.delete(20L, user);

        // Balance should increase by 150 (reversal of EXPENSE)
        assertThat(account.getBalance()).isEqualByComparingTo("1150");
        then(transactionRepository).should().delete(existing);
    }

    @Test
    @DisplayName("create throws ResourceNotFoundException for unknown account")
    void create_throwsForUnknownAccount() {
        TransactionRequest request = new TransactionRequest(
                99L, null, TransactionType.INCOME, BigDecimal.ONE, null, LocalDate.now());

        given(accountRepository.findByIdAndUser(99L, user)).willReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(request, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
