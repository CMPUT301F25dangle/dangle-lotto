package com.example.dangle_lotto;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.dangle_lotto.ui.login.LoginValidator;

public class LoginValidatorTest {

    @Test
    public void testEmptyEmailFails() {
        assertFalse(LoginValidator.isValidCredentials("", "password123"));
    }

    @Test
    public void testEmptyPasswordFails() {
        assertFalse(LoginValidator.isValidCredentials("user@example.com", ""));
    }

    @Test
    public void testNullEmailFails() {
        assertFalse(LoginValidator.isValidCredentials(null, "password123"));
    }

    @Test
    public void testNullPasswordFails() {
        assertFalse(LoginValidator.isValidCredentials("user@example.com", null));
    }

    @Test
    public void testValidCredentialsPass() {
        assertTrue(LoginValidator.isValidCredentials("user@example.com", "password123"));
    }

    @Test
    public void testWhitespaceOnlyFails() {
        assertFalse(LoginValidator.isValidCredentials("   ", "   "));
    }
}
