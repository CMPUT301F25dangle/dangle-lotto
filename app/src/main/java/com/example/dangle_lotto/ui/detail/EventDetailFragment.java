package com.example.dangle_lotto.ui.detail;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentEventDetailBinding;

import java.util.ArrayList;

public class EventDetailFragment extends Fragment {

    private String eventId;

    private FragmentEventDetailBinding binding;

    private boolean isSignedUp =  false;

    private Event selectedEvent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initialized binding
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initialize view model
        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        selectedEvent = userViewModel.getSelectedHomeEvent().getValue();

        final TextView textView = binding.tvTitle;
        textView.setText(selectedEvent.getName());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null){
            isSignedUp = savedInstanceState.getBoolean("isSignedUp", false);
        }

        updateSignUpButton();

        binding.btnSignUp.setOnClickListener(v -> {
            isSignedUp =! isSignedUp;
            updateSignUpButton();
        });

        binding.btnHelp.setOnClickListener(v -> showTermsDialog());
    }

    private void updateSignUpButton(){
        binding.btnSignUp.setText(isSignedUp?"Unregister":"Sign up");

    }

    private void showTermsDialog(){
        new AlertDialog.Builder(requireContext())
                .setTitle("Terms of Service")
                .setMessage("1) Be kind.\n2) Follow organizer rules.\n3) Respect the venue." +
                        "\n\nBy continuing, you agree to these terms.")
                .setPositiveButton("OK", (d,w)->d.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
