package com.example.dangle_lotto;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.dangle_lotto.ui.login.LoginFragment;
import com.example.dangle_lotto.ui.login.SignupFragment;
import com.google.firebase.FirebaseApp;

/**
 * LoginActivity - Activity for logging in.
 * <p>
 * Will open fragments for logging in and signing out
 *
 * @author Prem Elango
 * @version 1.0
 * @since 2025-10-25
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // opens login fragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.auth_fragment_container, new LoginFragment())
                    .commit();
        }
        FirebaseApp.initializeApp(this);
    }
}
