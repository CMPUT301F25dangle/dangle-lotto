package com.example.dangle_lotto.ui.create_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dangle_lotto.databinding.FragmentCreateEventBinding;

/**
 * CreateEventFragment - This fragment will be displayed whenever a GeneralUser
 * wants to organize an event.
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-05
 */
public class CreateEventFragment extends Fragment {
    private FragmentCreateEventBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
}
