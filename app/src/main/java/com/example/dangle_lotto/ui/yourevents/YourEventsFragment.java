package com.example.dangle_lotto.ui.yourevents;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dangle_lotto.databinding.FragmentYourEventsBinding;
import com.example.dangle_lotto.ui.dashboard.OrganizerEventDetailsEventFragment;

/**
 *
 */
public class YourEventsFragment extends Fragment {
    private FragmentYourEventsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentYourEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
}