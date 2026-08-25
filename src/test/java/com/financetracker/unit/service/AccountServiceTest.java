package com.financetracker.unit.service;

import com.financetracker.domain.entity.Account;
import com.financetracker.domain.entity.User;
import com.financetracker.domain.enums.AccountType;
import com.financetracker.dto.request.AccountRequest;
import com.financetracker.dto.response.AccountResponse;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.AccountMapper;
import com.financetracker.repository.AccountRepository;
import com.financetracker.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService unit tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@test.com").build();

        testAccount = Account.builder()
                .id(10L)
                .user(testUser)
                .name("My Checking")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.valueOf(1000))
                .currency("USD")
                .build();
    }

    @Test
    @DisplayName("findAll returns mapped list for the user")
    void findAll_returnsMappedList() {
        given(accountRepository.findAllByUser(testUser)).willReturn(List.of(testAccount));
        AccountResponse response = new AccountResponse(10L, "My Checking", AccountType.CHECKING,
                BigDecimal.valueOf(1000), "USD", null);
        given(accountMapper.toResponse(testAccount)).willReturn(response);

        List<AccountResponse> result = accountService.findAll(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("My Checking");
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException for unknown account")
    void findById_throwsWhenNotFound() {
        given(accountRepository.findByIdAndUser(99L, testUser)).willReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findById(99L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create persists account with correct initial balance")
    void create_persistsWithInitialBalance() {
        AccountRequest request = new AccountRequest("Savings", AccountType.SAVINGS, "USD", BigDecimal.valueOf(500));
        Account saved = Account.builder().id(11L).user(testUser).name("Savings")
                .type(AccountType.SAVINGS).balance(BigDecimal.valueOf(500)).currency("USD").build();
        AccountResponse expected = new AccountResponse(11L, "Savings", AccountType.SAVINGS,
                BigDecimal.valueOf(500), "USD", null);

        given(accountRepository.save(any(Account.class))).willReturn(saved);
        given(accountMapper.toResponse(saved)).willReturn(expected);

        AccountResponse result = accountService.create(request, testUser);

        assertThat(result.balance()).isEqualByComparingTo("500");
        then(accountRepository).should().save(any(Account.class));
    }

    @Test
    @DisplayName("create uses zero balance when initialBalance is null")
    void create_defaultsToZeroBalance() {
        AccountRequest request = new AccountRequest("Cash", AccountType.CASH, "USD", null);
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
        given(accountMapper.toResponse(any())).willReturn(
                new AccountResponse(1L, "Cash", AccountType.CASH, BigDecimal.ZERO, "USD", null));

        AccountResponse result = accountService.create(request, testUser);

        assertThat(result.balance()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("delete removes account when it belongs to the user")
    void delete_removesAccount() {
        given(accountRepository.findByIdAndUser(10L, testUser)).willReturn(Optional.of(testAccount));

        accountService.delete(10L, testUser);

        then(accountRepository).should().delete(testAccount);
    }

    @Test
    @DisplayName("adjustBalance correctly applies positive delta")
    void adjustBalance_appliesDelta() {
        given(accountRepository.findByIdAndUser(10L, testUser)).willReturn(Optional.of(testAccount));
        given(accountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        accountService.adjustBalance(10L, testUser, BigDecimal.valueOf(200));

        assertThat(testAccount.getBalance()).isEqualByComparingTo("1200");
    }
}
