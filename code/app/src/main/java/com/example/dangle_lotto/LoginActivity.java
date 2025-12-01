package com.example.dangle_lotto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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

    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        FirebaseApp.initializeApp(this);
        firebaseManager = FirebaseManager.getInstance();

        setContentView(R.layout.activity_login);

        // set to light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // attempt device-based auto login
        checkDeviceLogin(savedInstanceState);
    }

    /**
     * Checks if the current device is linked to a user in Firestore for auto-login.
     * <p>
     * If a user is found for the device ID, the app immediately launches MainActivity
     * with the associated UID. Otherwise, the login UI fragment is displayed.
     *
     * @param savedInstanceState Bundle used to restore the fragment state if needed.
     */
    private void checkDeviceLogin(Bundle savedInstanceState) {
        String deviceId = FirebaseManager.getDeviceId(this);
        firebaseManager.getUserByDeviceId(deviceId, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String uid) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("UID", uid);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.auth_fragment_container, new LoginFragment())
                            .commit();
                }
            }

            @Override
            public void onComplete() { }
        });
    }

}