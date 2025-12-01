package com.example.dangle_lotto.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.GeneralUser;
import com.example.dangle_lotto.LoginActivity;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentUserSettingBinding;
import com.example.dangle_lotto.ui.login.SimpleTextWatcher;
import com.google.firebase.auth.FirebaseAuth;

/**
 * UserSettingFragment - Fragment shows user settings.
 * NEED TO ADD CALLBACK TO UPDATE USER CUZ OTHERWISE DONT KNOW IF THERE ARE PROBLEMS IN IT
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
    private EditText nameEditText;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText passwordEditText;
    private CheckBox cbNotiOptOut;

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

        cbNotiOptOut = root.findViewById(R.id.cbNotiOptOut);
        cbNotiOptOut.setChecked(!user.getNotiStatus()); // invert initial status if needed

        cbNotiOptOut.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // isChecked = user wants to opt out, so notifications should be false
            boolean notiEnabled = !isChecked;
            user.setNotiStatus(notiEnabled);

            // Optionally update Firebase
            firebaseManager.updateUserNotificationStatus(user.getUid(), notiEnabled, new FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d("NotificationsFragment", "Notification status updated: " + notiEnabled);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("NotificationsFragment", "Failed to update notification status", e);
                }

                @Override
                public void onComplete() { }
            });
        });

        binding.logoutBtn.setOnClickListener(v -> {

            String uid = null;

            // Try getting UID from view model
            if (userViewModel.getUser().getValue() != null) {
                uid = userViewModel.getUser().getValue().getUid();
            }

            // If still null, fallback to FirebaseAuth current user
            if (uid == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            // Clear device binding if we have a UID
            Log.d("usersettingfragment", "before uid!=null");

            if (uid != null) {
                FirebaseManager.getInstance().getUsersReference().document(uid)
                        .update("DeviceId", null)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("Logout", "DeviceId cleared successfully");
                            } else {
                                Log.e("Logout", "Failed to clear DeviceId", task.getException());
                            }
                        });
            }


            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Go back to login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });



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
        nameEditText = binding.settingsFragmentNameInput;
        usernameEditText = binding.settingsFragmentUsernameInput;
        emailEditText = binding.settingsFragmentEmailInput;
        phoneEditText = binding.settingsFragmentPhoneInput;
        passwordEditText = binding.settingsFragmentPasswordInput;

        // setting edit text views with user info
        nameEditText.setText(user.getName());
        usernameEditText.setText((user.getUsername()));
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
        nameEditText.addTextChangedListener(watcher);
        usernameEditText.addTextChangedListener(watcher);
        emailEditText.addTextChangedListener(watcher);
        phoneEditText.addTextChangedListener(watcher);


        // clicking button to update user info
        binding.userSettingsUpdateButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();


            if (TextUtils.isEmpty(name)) {
                nameEditText.setError("Name required");
                return;
            }

            if (TextUtils.isEmpty(username)) {
                usernameEditText.setError("Username required");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email required");
                return;
            }
            // checks if email is valid format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Invalid email format");
                return;
            }

            if(TextUtils.isEmpty(password)){
                passwordEditText.setError("Password required");
                return;
            }

            // Need to hit the button twice (confirm system used)
            if (!confirmUpdate) {
                binding.userSettingsUpdateButton.setText("Confirm Update");
                confirmUpdate = true;
            } else {
                // updating user info
                firebaseManager.updateUser(user, name, username, email, phone,
                        user.getPhotoID(), password, new FirebaseCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                Toast toast = Toast.makeText(getActivity(), "User updated", Toast.LENGTH_SHORT);
                                toast.show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast toast = Toast.makeText(getActivity(), "Error updating user. Check password.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                );
                userViewModel.setUser(user);

                // unselecting edit text fields
                nameEditText.clearFocus();
                usernameEditText.clearFocus();
                emailEditText.clearFocus();
                phoneEditText.clearFocus();
                passwordEditText.clearFocus();

                // resetting confirm update
                confirmUpdate = false;

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
        String name = nameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        String originalPhone = user.getPhone() == null ? "" : user.getPhone();

        boolean same = name.equals(user.getName()) && email.equals(user.getEmail()) && phone.equals(originalPhone) && username.equals(user.getUsername());

        binding.userSettingsUpdateButton.setEnabled(!same);
    }
}