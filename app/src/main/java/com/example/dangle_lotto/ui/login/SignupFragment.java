package com.example.dangle_lotto.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.R;

public class SignupFragment extends Fragment {

    private EditText etSignupName, etSignupEmail, etSignupPhone, etSignupPassword;
    private Button btnSignUp;
    private FirebaseManager firebaseManager;


    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        etSignupName = view.findViewById(R.id.settings_fragment_username);
        etSignupEmail = view.findViewById(R.id.settings_fragment_email);
        etSignupPhone = view.findViewById(R.id.settings_fragment_phone_number);
        etSignupPassword = view.findViewById(R.id.etSignupPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp);

        firebaseManager = new FirebaseManager();

        btnSignUp.setOnClickListener(v -> registerUser());

        return view;
    }

    private void registerUser() {
        String name = etSignupName.getText().toString().trim();
        String email = etSignupEmail.getText().toString().trim();
        String phone = etSignupPhone.getText().toString().trim();
        String password = etSignupPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etSignupName.setError("Name required");
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

        firebaseManager.signUp(email, password, name, phone, "", false, new FirebaseCallback<String>() {
                    @Override
                    public void onComplete() {
                        btnSignUp.setEnabled(true);
                    }

                    @Override
                    public void onSuccess(String result) {
                        // can do whatever you want with uid here
                        String uid = result;

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.auth_fragment_container, new LoginFragment())
                                .commit();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getActivity(),
                                "Signup failed",
                                Toast.LENGTH_LONG).show();
                    }
                });

    }
}
