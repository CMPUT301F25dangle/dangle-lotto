package com.example.dangle_lotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.databinding.FragmentAdminViewUsersBinding;

/**
 * AdminViewUsersFragment - Fragment displays all user profiles
 * <p>
 * Admins can remove organizer privileges from users and delete accounts.
 * </p>
 *
 * @author Annie Ding
 * @version 1.-
 * @since 2025-11-22
 */
public class AdminViewUsersFragment extends Fragment {
    private FragmentAdminViewUsersBinding binding;
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        binding = FragmentAdminViewUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
}
