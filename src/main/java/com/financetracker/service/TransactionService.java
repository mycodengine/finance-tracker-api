package com.financetracker.service;

import com.financetracker.domain.entity.Account;
import com.financetracker.domain.entity.Category;
import com.financetracker.domain.entity.Transaction;
import com.financetracker.domain.entity.User;
import com.financetracker.domain.enums.TransactionType;
import com.financetracker.dto.request.TransactionRequest;
import com.financetracker.dto.response.PageResponse;
import com.financetracker.dto.response.TransactionResponse;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.TransactionMapper;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public PageResponse<TransactionResponse> findAll(
            User user, Long accountId, Long categoryId, TransactionType type,
            LocalDate startDate, LocalDate endDate, Pageable pageable) {

        Page<Transaction> page = transactionRepository.findByFilters(
                user.getId(), accountId, categoryId, type, startDate, endDate, pageable);

        return new PageResponse<>(
                page.getContent().stream().map(transactionMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    public TransactionResponse findById(Long id, User user) {
        return transactionMapper.toResponse(getOrThrow(id, user));
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request, User user) {
        Account account = accountRepository.findByIdAndUser(request.accountId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.accountId()));

        Category category = resolveCategory(request.categoryId());

        Transaction transaction = Transaction.builder()
                .account(account)
                .category(category)
                .type(request.type())
                .amount(request.amount())
                .description(request.description())
                .date(request.date())
                .build();

        transactionRepository.save(transaction);
        applyBalanceDelta(account, request.type(), request.amount());

        log.info("Created {} of {} on account {} for user {}",
                request.type(), request.amount(), account.getId(), user.getEmail());
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request, User user) {
        Transaction existing = getOrThrow(id, user);

        // Undo the old balance effect before applying the new one
        reverseBalanceDelta(existing.getAccount(), existing.getType(), existing.getAmount());

        Account newAccount = accountRepository.findByIdAndUser(request.accountId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.accountId()));

        existing.setAccount(newAccount);
        existing.setCategory(resolveCategory(request.categoryId()));
        existing.setType(request.type());
        existing.setAmount(request.amount());
        existing.setDescription(request.description());
        existing.setDate(request.date());

        applyBalanceDelta(newAccount, request.type(), request.amount());
        log.info("Updated transaction {} for user {}", id, user.getEmail());
        return transactionMapper.toResponse(transactionRepository.save(existing));
    }

    @Transactional
    public void delete(Long id, User user) {
        Transaction transaction = getOrThrow(id, user);
        reverseBalanceDelta(transaction.getAccount(), transaction.getType(), transaction.getAmount());
        transactionRepository.delete(transaction);
        log.info("Deleted transaction {} for user {}", id, user.getEmail());
    }

    // --- Helpers ---

    private void applyBalanceDelta(Account account, TransactionType type, BigDecimal amount) {
        BigDecimal delta = switch (type) {
            case INCOME   ->  amount;
            case EXPENSE  -> amount.negate();
            case TRANSFER -> BigDecimal.ZERO; // Transfer is recorded-only; balances adjusted manually
        };
        account.setBalance(account.getBalance().add(delta));
        accountRepository.save(account);
    }

    private void reverseBalanceDelta(Account account, TransactionType type, BigDecimal amount) {
        // Reversal is the inverse of the original effect
        applyBalanceDelta(account, type, amount.negate());
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private Transaction getOrThrow(Long id, User user) {
        return transactionRepository.findByIdAndAccountUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
    }
}
