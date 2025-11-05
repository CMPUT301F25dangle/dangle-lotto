package com.example.dangle_lotto.ui.dashboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentOrganizerEventDetailsEventBinding;

/**
 * This fragment opens inside of the OrganizerEventDetails fragment to display the details of the event
 */
public class OrganizerEventDetailsEventFragment extends Fragment {
    private FragmentOrganizerEventDetailsEventBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerEventDetailsEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }
}