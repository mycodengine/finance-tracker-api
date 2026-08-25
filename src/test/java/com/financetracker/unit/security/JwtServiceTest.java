package com.financetracker.unit.security;

import com.financetracker.domain.entity.User;
import com.financetracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService unit tests")
class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inject the required values via reflection (no Spring context needed)
        ReflectionTestUtils.setField(jwtService, "secret",
                "dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1kZXYtb25seQ==");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", 900_000L);

        user = User.builder().id(1L).email("user@test.com").password("pwd").build();
    }

    @Test
    @DisplayName("generated token contains the user's email as subject")
    void generateToken_containsEmailAsSubject() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("token is valid for the same user")
    void isTokenValid_trueForSameUser() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("token is invalid for a different user")
    void isTokenValid_falseForDifferentUser() {
        String token = jwtService.generateAccessToken(user);
        User other = User.builder().id(2L).email("other@test.com").password("pwd").build();

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    @DisplayName("expired token is reported as invalid")
    void expiredToken_isInvalid() {
        // Set expiry to 1ms so the token expires immediately
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", 1L);
        String token = jwtService.generateAccessToken(user);

        // Wait for expiry
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }
}
