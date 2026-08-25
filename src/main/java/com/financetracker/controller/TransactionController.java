package com.financetracker.controller;

import com.financetracker.domain.entity.User;
import com.financetracker.domain.enums.TransactionType;
import com.financetracker.dto.request.TransactionRequest;
import com.financetracker.dto.response.PageResponse;
import com.financetracker.dto.response.TransactionResponse;
import com.financetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Record and manage income/expense transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List transactions with optional filters and pagination")
    public PageResponse<TransactionResponse> findAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        return transactionService.findAll(user, accountId, categoryId, type, startDate, endDate, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single transaction by ID")
    public TransactionResponse findById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return transactionService.findById(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new transaction")
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request,
                                      @AuthenticationPrincipal User user) {
        return transactionService.create(request, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing transaction")
    public TransactionResponse update(@PathVariable Long id,
                                      @Valid @RequestBody TransactionRequest request,
                                      @AuthenticationPrincipal User user) {
        return transactionService.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a transaction and reverse its balance effect")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        transactionService.delete(id, user);
    }
}
