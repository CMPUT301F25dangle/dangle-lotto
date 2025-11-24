package com.example.dangle_lotto.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.GeneralUser;
import com.example.dangle_lotto.LoginActivity;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentUserSettingBinding;
import com.example.dangle_lotto.ui.login.SimpleTextWatcher;

/**
 * UserSettingFragment - Fragment shows user settings.
 * NEED TO ADD CALLBACK TO UPDATE USER CUZ OTHERWISE DONT KNOW IF THERE ARE PROBLEMS IN IT
 * NEED TO CHANGE THE EMAIL IN FIREBASE AUTH
 * @author Aditya Soni
 * @version 1.0
 * @since 2025-11-06
 */
public class UserSettingFragment extends Fragment {
    private FragmentUserSettingBinding binding;
    private UserViewModel userViewModel;
    private FirebaseManager firebaseManager;
    private GeneralUser user;
    private boolean confirmDelete = false;
    private boolean confirmUpdate = false;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // initializing binding
        binding = FragmentUserSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initializing firebase
        firebaseManager = FirebaseManager.getInstance();

        // initializing view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // getting user from view model
        user = userViewModel.getUser().getValue();

        // making back button actually take you to previous fragment
        binding.settingsFragmentBackButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // making delete button delete the user
        binding.userSettingsDeleteButton.setOnClickListener(v -> {
            if (!confirmDelete) {
                binding.userSettingsDeleteButton.setText("Confirm Delete");
                confirmDelete = true;
            } else {
                user.delete();
                Toast.makeText(getActivity(), "User deleted", Toast.LENGTH_SHORT).show();
                // switches to login screen
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        // attaching edit text views
        usernameEditText = binding.settingsFragmentUsername;
        emailEditText = binding.settingsFragmentEmail;
        phoneEditText = binding.settingsFragmentPhoneNumber;

        // setting edit text views with user info
        usernameEditText.setText(user.getName());
        emailEditText.setText(user.getEmail());
        phoneEditText.setText(user.getPhone());

        // updating button state
        updateButtonState();

        // setting up text watcher
        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }
        };
        usernameEditText.addTextChangedListener(watcher);
        emailEditText.addTextChangedListener(watcher);
        phoneEditText.addTextChangedListener(watcher);

        // clicking button to update user info
        binding.userSettingsUpdateButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                usernameEditText.setError("Name required");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email required");
                return;
            }

            // Need to hit the button twice (confirm system used)
            if (!confirmUpdate) {
                binding.userSettingsUpdateButton.setText("Confirm Update");
                confirmUpdate = true;
            } else {
                user.setName(username);
                user.setEmail(email);
                user.setPhone(phone);

                // updating user info
                firebaseManager.updateUser(user);
                userViewModel.setUser(user);

                // unselecting edit text fields
                usernameEditText.clearFocus();
                emailEditText.clearFocus();
                phoneEditText.clearFocus();

                // updating button state
                updateButtonState();
                binding.userSettingsUpdateButton.setText("Update Profile");
            }
        });

        return root;
    }

    /**
     * Updates the button state based on the current user information.
     */
    private void updateButtonState() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        String originalPhone = user.getPhone() == null ? "" : user.getPhone();

        boolean same = username.equals(user.getName()) && email.equals(user.getEmail()) && phone.equals(originalPhone);

        binding.userSettingsUpdateButton.setEnabled(!same);
    }
}