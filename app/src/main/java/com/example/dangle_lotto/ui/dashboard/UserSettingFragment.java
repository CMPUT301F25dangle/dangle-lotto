package com.example.dangle_lotto.ui.dashboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentUserSettingBinding;

/**
 * UserSettingFragment - Fragment shows user settings.
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 2025-11-06
 */
public class UserSettingFragment extends Fragment {
    private FragmentUserSettingBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
}