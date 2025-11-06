package com.example.dangle_lotto.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.GeneralUser;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.ui.login.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupFragment extends Fragment {

    private EditText etSignupName, etSignupEmail, etSignupPhone, etSignupPassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        etSignupName = view.findViewById(R.id.etSignupName);
        etSignupEmail = view.findViewById(R.id.etSignupEmail);
        etSignupPhone = view.findViewById(R.id.etSignupPhone);
        etSignupPassword = view.findViewById(R.id.etSignupPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();

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

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnSignUp.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        Toast.makeText(getActivity(), "Signup successful! Please log in", Toast.LENGTH_SHORT).show();
                        // code to make User object
                        if (user != null) {
                            String uid = user.getUid();
                            String photo_id = "";
                            FirebaseManager firebaseManager = new FirebaseManager();

                            User newUser = firebaseManager.createNewUser(uid, name, email, phone, photo_id, false);
                        }
                        // switch to login fragment
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.auth_fragment_container, new LoginFragment())
                                .commit();

                    } else {
                        Toast.makeText(getActivity(),
                                "Signup failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
