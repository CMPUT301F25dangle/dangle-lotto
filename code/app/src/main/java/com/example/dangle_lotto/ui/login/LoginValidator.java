package com.example.dangle_lotto.ui.login;

/**
 * Helper class to validate login credentials.
 */
public class LoginValidator {

    /**
     * Validates email and password input for login.
     *
     * @param email    user email
     * @param password user password
     * @return true if both are non-empty
     */
    public static boolean isValidCredentials(String email, String password) {
        if (email == null || email.trim().isEmpty()) return false;
        if (password == null || password.trim().isEmpty()) return false;
        return true;
    }
}
