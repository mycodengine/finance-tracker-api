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

@DisplayName("Report API integration tests")
class ReportControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String bearerToken;
    private Long accountId;
    private int currentYear;
    private int currentMonth;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("Reporter", "report@it.com", "password123"))));

        String loginBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("report@it.com", "password123"))))
                .andReturn().getResponse().getContentAsString();

        bearerToken = "Bearer " + objectMapper.readTree(loginBody).get("accessToken").asText();

        String accBody = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AccountRequest("Report Account", AccountType.CHECKING, "USD", BigDecimal.valueOf(0)))))
                .andReturn().getResponse().getContentAsString();

        accountId = objectMapper.readTree(accBody).get("id").asLong();
        currentYear = LocalDate.now().getYear();
        currentMonth = LocalDate.now().getMonthValue();

        // Seed transactions for current month
        postTransaction(TransactionType.INCOME, BigDecimal.valueOf(3000), "Salary");
        postTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(1200), "Rent");
        postTransaction(TransactionType.EXPENSE, BigDecimal.valueOf(400), "Groceries");
    }

    @Test
    @DisplayName("GET /reports/summary — returns correct income and expense totals")
    void getMonthlySummary_returnsCorrectTotals() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary")
                        .param("year", String.valueOf(currentYear))
                        .param("month", String.valueOf(currentMonth))
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(3000))
                .andExpect(jsonPath("$.totalExpense").value(1600))
                .andExpect(jsonPath("$.netBalance").value(1400));
    }

    @Test
    @DisplayName("GET /reports/summary — defaults to current month when params omitted")
    void getMonthlySummary_defaultsToCurrentMonth() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(currentYear))
                .andExpect(jsonPath("$.month").value(currentMonth));
    }

    @Test
    @DisplayName("GET /reports/by-category — returns non-empty breakdown")
    void getCategoryBreakdown_returnsBreakdown() throws Exception {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        mockMvc.perform(get("/api/v1/reports/by-category")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString())
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    private void postTransaction(TransactionType type, BigDecimal amount, String desc) throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .header("Authorization", bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new TransactionRequest(accountId, null, type, amount, desc, LocalDate.now()))));
    }
}
