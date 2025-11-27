package com.example.dangle_lotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.databinding.FragmentAdminViewEventsBinding;

/**
 * AdminViewEventsFragment - This fragment is displayed when an admin user wants
 * to view all the events. Admins can view events and delete any events that
 * go against app policy.
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-22
 */
public class AdminViewEventsFragment extends Fragment {
    private FragmentAdminViewEventsBinding binding;
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private int selectedEventIdx = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentAdminViewEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;

    }
}
