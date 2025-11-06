package com.example.dangle_lotto.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.GeneralUser;
import com.example.dangle_lotto.LoginActivity;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentUserSettingBinding;

/**
 *
 */
public class UserSettingFragment extends Fragment {
    private FragmentUserSettingBinding binding;
    private UserViewModel userViewModel;
    private FirebaseManager firebaseManager;
    private GeneralUser user;
    private boolean confirm = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // initializing binding
        binding = FragmentUserSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initializing firebase
        firebaseManager = new FirebaseManager();

        // initializing view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // getting user from view model
        user = userViewModel.getUser().getValue();

        // making back button actually take you to previous fragment
        binding.settingsFragmentBackButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // setting edit text views with user info
        binding.settingsFragmentUsername.setText(user.getName());
        binding.settingsFragmentEmail.setText(user.getEmail());
        binding.settingsFragmentPhoneNumber.setText(user.getPhone());

        // making delete button delete the user
        binding.button.setOnClickListener(v -> {
            if (!confirm) {
                binding.button.setText("Confirm Delete");
                confirm = true;
            } else {
                user.delete();
                Toast.makeText(getActivity(), "User deleted", Toast.LENGTH_SHORT).show();
                // switches to login screen
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        return root;
    }
}