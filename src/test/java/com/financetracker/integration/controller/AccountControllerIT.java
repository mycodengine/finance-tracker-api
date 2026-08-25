package com.financetracker.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.AbstractIntegrationTest;
import com.financetracker.domain.enums.AccountType;
import com.financetracker.dto.request.AccountRequest;
import com.financetracker.dto.request.LoginRequest;
import com.financetracker.dto.request.RegisterRequest;
import com.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Account API integration tests")
class AccountControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String bearerToken;

    @BeforeEach
    void setUpUser() throws Exception {
        userRepository.deleteAll();

        // Register and login to get a token
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("Test User", "test@it.com", "password123"))));

        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("test@it.com", "password123"))))
                .andReturn().getResponse().getContentAsString();

        bearerToken = "Bearer " + objectMapper.readTree(body).get("accessToken").asText();
    }

    @Test
    @DisplayName("POST /accounts — creates account and returns 201")
    void createAccount_returns201() throws Exception {
        AccountRequest request = new AccountRequest("My Savings", AccountType.SAVINGS, "USD", BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Savings"))
                .andExpect(jsonPath("$.balance").value(500))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("GET /accounts — returns list of user accounts")
    void listAccounts_returnsAll() throws Exception {
        AccountRequest req = new AccountRequest("Checking", AccountType.CHECKING, "USD", BigDecimal.ZERO);
        mockMvc.perform(post("/api/v1/accounts")
                .header("Authorization", bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        mockMvc.perform(get("/api/v1/accounts")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name").value("Checking"));
    }

    @Test
    @DisplayName("GET /accounts/{id} — returns 404 for unknown account")
    void getAccount_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/99999")
                        .header("Authorization", bearerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /accounts/{id} — removes account")
    void deleteAccount_returns204() throws Exception {
        String body = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AccountRequest("ToDelete", AccountType.CASH, "USD", BigDecimal.ZERO))))
                .andReturn().getResponse().getContentAsString();

        Long accountId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(delete("/api/v1/accounts/" + accountId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                        .header("Authorization", bearerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Unauthenticated request returns 401")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isUnauthorized());
    }
}
