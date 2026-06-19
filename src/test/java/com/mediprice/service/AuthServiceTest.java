package com.mediprice.service;

import com.mediprice.dao.UserDAO;
import com.mediprice.model.User;
import com.mediprice.util.PasswordUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private AuthService authService;

    // ===== REGISTRATION TESTS =====

    @Test
    @DisplayName("Register with valid data should succeed")
    void register_validData_returnsUser() throws Exception {
        when(userDAO.existsByUsername("john")).thenReturn(false);
        when(userDAO.existsByEmail("john@test.com")).thenReturn(false);
        when(userDAO.insert(any(User.class))).thenReturn(42);

        User result = authService.register("john", "john@test.com", "password123", "password123");

        assertNotNull(result);
        assertEquals("john", result.getUsername());
        assertEquals("john@test.com", result.getEmail());
        assertEquals("USER", result.getRole());
        assertEquals(42, result.getId());
    }

    @Test
    @DisplayName("Register with invalid username should throw ServiceException")
    void register_invalidUsername_throwsException() {
        assertThrows(ServiceException.class,
                () -> authService.register("ab", "john@test.com", "password123", "password123"),
                "Username too short should fail");

        assertThrows(ServiceException.class,
                () -> authService.register("john doe", "john@test.com", "password123", "password123"),
                "Username with space should fail");
    }

    @Test
    @DisplayName("Register with invalid email should throw ServiceException")
    void register_invalidEmail_throwsException() {
        assertThrows(ServiceException.class,
                () -> authService.register("john", "not-an-email", "password123", "password123"));
    }

    @Test
    @DisplayName("Register with short password should throw ServiceException")
    void register_shortPassword_throwsException() {
        assertThrows(ServiceException.class,
                () -> authService.register("john", "john@test.com", "abc", "abc"));
    }

    @Test
    @DisplayName("Register with mismatched passwords should throw ServiceException")
    void register_passwordMismatch_throwsException() {
        assertThrows(ServiceException.class,
                () -> authService.register("john", "john@test.com", "password123", "different"));
    }

    @Test
    @DisplayName("Register with duplicate username should throw ServiceException")
    void register_duplicateUsername_throwsException() throws Exception {
        when(userDAO.existsByUsername("john")).thenReturn(true);
        assertThrows(ServiceException.class,
                () -> authService.register("john", "john@test.com", "password123", "password123"));
    }

    @Test
    @DisplayName("Register with duplicate email should throw ServiceException")
    void register_duplicateEmail_throwsException() throws Exception {
        when(userDAO.existsByUsername("john")).thenReturn(false);
        when(userDAO.existsByEmail("john@test.com")).thenReturn(true);
        assertThrows(ServiceException.class,
                () -> authService.register("john", "john@test.com", "password123", "password123"));
    }

    // ===== LOGIN TESTS =====

    @Test
    @DisplayName("Login with correct credentials should return user")
    void login_correctCredentials_returnsUser() throws Exception {
        User user = buildActiveUser("alice", "password123");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));

        User result = authService.login("alice", "password123");

        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        verify(userDAO).resetFailedAttempts(user.getId());
    }

    @Test
    @DisplayName("Login with wrong password should throw ServiceException")
    void login_wrongPassword_throwsException() throws Exception {
        User user = buildActiveUser("alice", "password123");
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThrows(ServiceException.class, () -> authService.login("alice", "wrongpassword"));
    }

    @Test
    @DisplayName("Login with unknown username should throw ServiceException")
    void login_unknownUsername_throwsException() throws Exception {
        when(userDAO.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThrows(ServiceException.class, () -> authService.login("nobody", "any"));
    }

    @Test
    @DisplayName("Login with locked account should throw ServiceException")
    void login_lockedAccount_throwsException() throws Exception {
        User user = buildActiveUser("alice", "password123");
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(userDAO.findByUsername("alice")).thenReturn(Optional.of(user));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.login("alice", "password123"));
        assertTrue(ex.getMessage().contains("locked"));
    }

    @Test
    @DisplayName("Login with empty credentials should throw ServiceException")
    void login_emptyCredentials_throwsException() {
        assertThrows(ServiceException.class, () -> authService.login("", "password"));
        assertThrows(ServiceException.class, () -> authService.login("user", ""));
    }

    // ===== HELPERS =====

    private User buildActiveUser(String username, String plainPassword) {
        User user = new User();
        user.setId(1);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        user.setRole("USER");
        user.setFailedAttempts(0);
        return user;
    }
}
