package com.financetracker.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.AbstractIntegrationTest;
import com.financetracker.domain.enums.AccountType;
import com.financetracker.domain.enums.TransactionType;
import com.financetracker.dto.request.AccountRequest;
import com.financetracker.dto.request.LoginRequest;
import com.financetracker.dto.request.RegisterRequest;
import com.financetracker.dto.request.TransactionRequest;
import com.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Transaction API integration tests")
class TransactionControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String bearerToken;
    private Long accountId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("TxUser", "tx@it.com", "password123"))));

        String loginBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("tx@it.com", "password123"))))
                .andReturn().getResponse().getContentAsString();

        bearerToken = "Bearer " + objectMapper.readTree(loginBody).get("accessToken").asText();

        // Create an account to associate transactions with
        String accBody = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AccountRequest("Main Account", AccountType.CHECKING, "USD", BigDecimal.valueOf(2000)))))
                .andReturn().getResponse().getContentAsString();

        accountId = objectMapper.readTree(accBody).get("id").asLong();
    }

    @Test
    @DisplayName("POST /transactions — INCOME increases account balance")
    void createIncome_increasesBalance() throws Exception {
        TransactionRequest request = new TransactionRequest(
                accountId, null, TransactionType.INCOME, BigDecimal.valueOf(500), "Bonus", LocalDate.now());

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.amount").value(500));

        // Verify account balance updated
        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                        .header("Authorization", bearerToken))
                .andExpect(jsonPath("$.balance").value(2500));
    }

    @Test
    @DisplayName("DELETE /transactions/{id} — reverses account balance")
    void deleteTransaction_reversesBalance() throws Exception {
        TransactionRequest request = new TransactionRequest(
                accountId, null, TransactionType.EXPENSE, BigDecimal.valueOf(300), "Rent", LocalDate.now());

        String txBody = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long txId = objectMapper.readTree(txBody).get("id").asLong();

        // Balance should be 2000 - 300 = 1700
        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                        .header("Authorization", bearerToken))
                .andExpect(jsonPath("$.balance").value(1700));

        mockMvc.perform(delete("/api/v1/transactions/" + txId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isNoContent());

        // Balance should revert to 2000
        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                        .header("Authorization", bearerToken))
                .andExpect(jsonPath("$.balance").value(2000));
    }

    @Test
    @DisplayName("GET /transactions — supports filtering by type")
    void listTransactions_filterByType() throws Exception {
        // Create one INCOME and one EXPENSE
        mockMvc.perform(post("/api/v1/transactions")
                .header("Authorization", bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new TransactionRequest(accountId, null, TransactionType.INCOME,
                                BigDecimal.valueOf(100), "Income1", LocalDate.now()))));

        mockMvc.perform(post("/api/v1/transactions")
                .header("Authorization", bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new TransactionRequest(accountId, null, TransactionType.EXPENSE,
                                BigDecimal.valueOf(50), "Expense1", LocalDate.now()))));

        mockMvc.perform(get("/api/v1/transactions?type=INCOME")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].type", everyItem(is("INCOME"))));
    }
}
