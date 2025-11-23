package com.example.dangle_lotto.ui.login;

/**
 * Creates a text watcher that can be used to actively watch edit text fields and do things.
 * <p>
 * Methods implemented for specific use case
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 2025-11-06
 */
public abstract class SimpleTextWatcher implements android.text.TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void afterTextChanged(android.text.Editable s) {}
}

