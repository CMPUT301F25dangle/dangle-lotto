package com.example.dangle_lotto.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dangle_lotto.AdminActivity;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.MainActivity;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
/**
 * LoginFragment - Fragment handles user authentication.
 * <p>
 * Allows users to log in using email and password through Firebase Authentication.
 * On successful login, navigates to MainActivity; users can also switch to the signup
 * screen from this fragment.
 *
 * @author Prem Elango
 * @version 1.3
 * @since 2025-10-30
 */

public class LoginFragment extends Fragment {

    private EditText etLoginEmail, etLoginPassword;
    private Button btnLogin;
    private CheckBox cbRememberMe;
    private TextView tvToSignUp;
    private FirebaseManager firebaseManager;

    private Button btnDeviceSignIn;
    private FirebaseAuth auth;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        auth = FirebaseAuth.getInstance();

        etLoginEmail = view.findViewById(R.id.etLoginEmail);
        etLoginPassword = view.findViewById(R.id.etLoginPassword);
        cbRememberMe = view.findViewById(R.id.cbRememberMe);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvToSignUp = view.findViewById(R.id.tvGoToSignup);

        firebaseManager = FirebaseManager.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());
        tvToSignUp.setOnClickListener(v -> switchToSignUp());

        return view;
    }

    // Load the new User and check if they are admin then redirect them appropriately
    private void loadUser(String uid){
        firebaseManager.getUser(uid, new FirebaseCallback<User>() {

                    @Override
                    public void onSuccess(User result) {
                        Intent intent = null;
                        if (result.isAdmin())
                            intent = new Intent(getActivity(), AdminActivity.class);
                        else
                            intent = new Intent(getActivity(), MainActivity.class);
                        intent.putExtra("UID", uid);
                        startActivity(intent);
                        Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                        requireActivity().finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("LoginFragment", "Error loading user: " + e.getMessage());
                        Toast.makeText(getActivity(), "Error loading user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

            /**
     * loginUser - Handles user login using Firebase Authentication.
     * <p>
     * Validates the entered email and password, attempts to sign in through Firebase,
     * and navigates to MainActivity upon successful authentication.
     * Displays error messages for invalid credentials or failed login attempts.
     */

    private void loginUser() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etLoginEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError("Password is required");
            return;
        }

        btnLogin.setEnabled(false);

        firebaseManager.signIn(email, password, new FirebaseCallback<String>(){
            @Override
            public void onComplete() {
                btnLogin.setEnabled(true);
            }

            @Override
            public void onSuccess(String result) {
                if (cbRememberMe.isChecked()) {
                    saveDeviceIdToUser(result);
                }
                loadUser(result);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * switchToSignUp - Navigates the user to the signup screen.
     * <p>
     * Replaces the current fragment with {@link SignupFragment} to allow new user registration.
     */

    private void switchToSignUp() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, new SignupFragment())
                .addToBackStack(null)
                .commit();
    }

    private void saveDeviceIdToUser(String uid) {
        String deviceId = FirebaseManager.getDeviceId(requireContext());

        firebaseManager.setDeviceIdForUser(uid, deviceId, new FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d("saveDeviceIdToUser", "DeviceId successfully saved: " + deviceId);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("saveDeviceIdToUser", "Failed to save DeviceId", e);
            }

            @Override
            public void onComplete() {
                Log.d("saveDeviceIdToUser", "DeviceId save operation completed");
            }
        });
    }


}
