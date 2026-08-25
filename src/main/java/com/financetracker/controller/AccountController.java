package com.financetracker.controller;

import com.financetracker.domain.entity.User;
import com.financetracker.dto.request.AccountRequest;
import com.financetracker.dto.response.AccountResponse;
import com.financetracker.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Manage financial accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "List all accounts for the authenticated user")
    public List<AccountResponse> findAll(@AuthenticationPrincipal User user) {
        return accountService.findAll(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single account by ID")
    public AccountResponse findById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return accountService.findById(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new account")
    public AccountResponse create(@Valid @RequestBody AccountRequest request,
                                  @AuthenticationPrincipal User user) {
        return accountService.create(request, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing account")
    public AccountResponse update(@PathVariable Long id,
                                  @Valid @RequestBody AccountRequest request,
                                  @AuthenticationPrincipal User user) {
        return accountService.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an account and all its transactions")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        accountService.delete(id, user);
    }
}
