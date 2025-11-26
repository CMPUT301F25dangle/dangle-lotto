package com.example.dangle_lotto.ui.dashboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentOrganizerEventDetailsMapBinding;

/**
 * This fragment opens inside of the OrganizerEventDetails fragment to display the map of users
 */
public class OrganizerEventDetailsMapFragment extends Fragment {
    private FragmentOrganizerEventDetailsMapBinding binding;
    private UserViewModel userViewModel;
    private Event event;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerEventDetailsMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
}