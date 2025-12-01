package com.example.dangle_lotto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean remember = prefs.getBoolean("rememberMe", true);

        if (firebaseManager.getAuth().getCurrentUser() != null && remember) {
            loadUser(firebaseManager.getAuth().getCurrentUser().getUid());
            return;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.auth_fragment_container, new LoginFragment())
                    .commit();
        }

        // attempt device-based auto login
//        checkDeviceLogin(savedInstanceState);
    }

    /**
     * Loads a user's profile from Firebase using the provided UID and navigates
     * to the appropriate activity based on their role. If the user is an admin,
     * they are redirected to {@link AdminActivity}; otherwise, they are taken to
     * {@link MainActivity}. Displays a success message on completion and finishes
     * the current activity.
     *
     * @param uid The unique identifier of the user whose data should be loaded.
     */
    private void loadUser(String uid){
        firebaseManager.getUser(uid, new FirebaseCallback<User>() {

            @Override
            public void onSuccess(User result) {
                Intent intent = null;
                if (result.isAdmin())
                    intent = new Intent(LoginActivity.this, AdminActivity.class);
                else
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("UID", uid);
                startActivity(intent);
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("LoginFragment", "Error loading user: " + e.getMessage());
            }
        });
    }
}