package com.example.dangle_lotto.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dangle_lotto.R;
import com.example.dangle_lotto.ui.login.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupFragment extends Fragment {

    private EditText etSignupUsername, etSignupEmail, etSignupPhone, etSignupPassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        etSignupUsername = view.findViewById(R.id.etSignupUsername);
        etSignupEmail = view.findViewById(R.id.etSignupEmail);
        etSignupPhone = view.findViewById(R.id.etSignupPhone);
        etSignupPassword = view.findViewById(R.id.etSignupPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(v -> registerUser());

        return view;
    }

    private void registerUser() {
        String username = etSignupUsername.getText().toString().trim();
        String email = etSignupEmail.getText().toString().trim();
        String phone = etSignupPhone.getText().toString().trim();
        String password = etSignupPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etSignupUsername.setError("Username required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etSignupEmail.setError("Email required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etSignupPassword.setError("Password required");
            return;
        }

        btnSignUp.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnSignUp.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Optional: set display name
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build();
                            user.updateProfile(profileUpdates);
                        }

                        Toast.makeText(getActivity(), "Signup successful!", Toast.LENGTH_SHORT).show();

                        // Switch to login fragment
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.auth_fragment_container, new LoginFragment())
                                .commit();

                    } else {
                        Toast.makeText(getActivity(),
                                "Signup failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
