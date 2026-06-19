package com.mediprice.service;

import com.mediprice.util.PasswordUtil;
import com.mediprice.util.ValidationUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtil and ValidationUtil.
 */
class UtilsTest {

    // ===== PASSWORD UTIL =====

    @Test
    @DisplayName("Hashed password should not equal plain text")
    void hashPassword_differentFromPlain() {
        String hash = PasswordUtil.hashPassword("mypassword");
        assertNotEquals("mypassword", hash);
    }

    @Test
    @DisplayName("BCrypt hash should start with $2a$")
    void hashPassword_isBCryptFormat() {
        String hash = PasswordUtil.hashPassword("mypassword");
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
    }

    @Test
    @DisplayName("verifyPassword should return true for correct password")
    void verifyPassword_correct_returnsTrue() {
        String hash = PasswordUtil.hashPassword("secret123");
        assertTrue(PasswordUtil.verifyPassword("secret123", hash));
    }

    @Test
    @DisplayName("verifyPassword should return false for wrong password")
    void verifyPassword_wrong_returnsFalse() {
        String hash = PasswordUtil.hashPassword("secret123");
        assertFalse(PasswordUtil.verifyPassword("wrongpassword", hash));
    }

    @Test
    @DisplayName("hashPassword with null should throw")
    void hashPassword_null_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
    }

    @Test
    @DisplayName("verifyPassword with null should return false")
    void verifyPassword_null_returnsFalse() {
        assertFalse(PasswordUtil.verifyPassword(null, "hash"));
        assertFalse(PasswordUtil.verifyPassword("password", null));
    }

    // ===== VALIDATION UTIL =====

    @Test
    @DisplayName("Valid emails should pass")
    void isValidEmail_validEmails() {
        assertTrue(ValidationUtil.isValidEmail("user@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user.name+tag@domain.co.uk"));
        assertTrue(ValidationUtil.isValidEmail("user123@test.org"));
    }

    @Test
    @DisplayName("Invalid emails should fail")
    void isValidEmail_invalidEmails() {
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail("not-an-email"));
        assertFalse(ValidationUtil.isValidEmail("@nodomain.com"));
        assertFalse(ValidationUtil.isValidEmail("user@"));
    }

    @Test
    @DisplayName("Valid usernames should pass")
    void isValidUsername_validUsernames() {
        assertTrue(ValidationUtil.isValidUsername("john"));
        assertTrue(ValidationUtil.isValidUsername("john_doe"));
        assertTrue(ValidationUtil.isValidUsername("User123"));
        assertTrue(ValidationUtil.isValidUsername("abc"));
    }

    @Test
    @DisplayName("Invalid usernames should fail")
    void isValidUsername_invalidUsernames() {
        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername("ab"));  // too short
        assertFalse(ValidationUtil.isValidUsername("john doe"));  // space
        assertFalse(ValidationUtil.isValidUsername("user@name"));  // special char
        assertFalse(ValidationUtil.isValidUsername("a".repeat(31)));  // too long
    }

    @Test
    @DisplayName("Password validation")
    void isValidPassword() {
        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertTrue(ValidationUtil.isValidPassword("mypassword"));
        assertFalse(ValidationUtil.isValidPassword("12345"));  // too short
        assertFalse(ValidationUtil.isValidPassword(null));
    }

    @Test
    @DisplayName("Valid coordinates should pass")
    void isValidCoordinate() {
        assertTrue(ValidationUtil.isValidCoordinate(52.2297, 21.0122));
        assertTrue(ValidationUtil.isValidCoordinate(-90.0, -180.0));
        assertTrue(ValidationUtil.isValidCoordinate(90.0, 180.0));
        assertFalse(ValidationUtil.isValidCoordinate(91.0, 21.0));  // lat > 90
        assertFalse(ValidationUtil.isValidCoordinate(52.0, 181.0));  // lon > 180
    }

    @Test
    @DisplayName("Valid prices should pass")
    void isValidPrice() {
        assertTrue(ValidationUtil.isValidPrice(0.0));
        assertTrue(ValidationUtil.isValidPrice(9.99));
        assertTrue(ValidationUtil.isValidPrice(99999.99));
        assertFalse(ValidationUtil.isValidPrice(-1.0));
        assertFalse(ValidationUtil.isValidPrice(100000.0));
    }
}
