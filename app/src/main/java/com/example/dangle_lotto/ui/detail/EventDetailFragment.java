package com.example.dangle_lotto.ui.detail;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dangle_lotto.databinding.FragmentEventDetailBinding;

public class EventDetailFragment extends Fragment {

    private String eventId;

    private FragmentEventDetailBinding binding;

    // Mock Data
    private String title = "Event Name";
    private String deadline = "Nov 24, 11pm";
    private String description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
            "sed do eiusmod tempor incididunt ut labore...";
    private int signedUp = 10;
    private Integer capacity = 50; // set to null for uncapped
    private boolean isSignedUp = false;
    // ----------------------------


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EventDetailViewModel EventDetailViewModel =
                new ViewModelProvider(this).get(EventDetailViewModel.class);

        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        /*
         final TextView textView = binding.textEventDetail;
         EventDetailViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        */
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
