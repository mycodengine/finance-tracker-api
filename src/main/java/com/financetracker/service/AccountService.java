package com.financetracker.service;

import com.financetracker.domain.entity.Account;
import com.financetracker.domain.entity.User;
import com.financetracker.dto.request.AccountRequest;
import com.financetracker.dto.response.AccountResponse;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.AccountMapper;
import com.financetracker.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public List<AccountResponse> findAll(User user) {
        return accountRepository.findAllByUser(user).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    public AccountResponse findById(Long id, User user) {
        return accountMapper.toResponse(getOrThrow(id, user));
    }

    @Transactional
    public AccountResponse create(AccountRequest request, User user) {
        Account account = Account.builder()
                .user(user)
                .name(request.name())
                .type(request.type())
                .currency(request.currency())
                .balance(request.initialBalance() != null ? request.initialBalance() : BigDecimal.ZERO)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account '{}' created for user {}", saved.getName(), user.getEmail());
        return accountMapper.toResponse(saved);
    }

    @Transactional
    public AccountResponse update(Long id, AccountRequest request, User user) {
        Account account = getOrThrow(id, user);
        account.setName(request.name());
        account.setType(request.type());
        account.setCurrency(request.currency());

        log.info("Account {} updated for user {}", id, user.getEmail());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public void delete(Long id, User user) {
        Account account = getOrThrow(id, user);
        accountRepository.delete(account);
        log.info("Account {} deleted for user {}", id, user.getEmail());
    }

    /** Package-visible helper used by TransactionService to adjust balance after a transaction. */
    @Transactional
    public void adjustBalance(Long accountId, User user, BigDecimal delta) {
        Account account = getOrThrow(accountId, user);
        account.setBalance(account.getBalance().add(delta));
        accountRepository.save(account);
        log.debug("Account {} balance adjusted by {} for user {}", accountId, delta, user.getEmail());
    }

    private Account getOrThrow(Long id, User user) {
        return accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }
}
