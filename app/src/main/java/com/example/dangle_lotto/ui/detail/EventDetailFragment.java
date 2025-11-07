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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dangle_lotto.FirebaseManager;
import com.google.firebase.Timestamp;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentEventDetailBinding;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventDetailFragment extends Fragment {
    private FragmentEventDetailBinding binding;
    private FirebaseManager firebaseManager;
    private UserViewModel userViewModel;
    private boolean isAttendee = false;
    private boolean isSignedUp = false;
    private boolean isChosen = false;
    private boolean isWaiting = false;
    private boolean isCancelled = false;
    private boolean postDraw = false;
    private Event selectedEvent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initialized binding
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initializing firebase manager
        firebaseManager = new FirebaseManager();

        // initialize view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        selectedEvent = userViewModel.getSelectedHomeEvent().getValue();

        // Sets the Event Title
        final TextView textView = binding.eventTitle;
        textView.setText(selectedEvent.getName());

        // Sets the Event Description
        final TextView textView1 = binding.eventDescription;
        textView1.setText(selectedEvent.getDescription());

        // Sets the Deadline
        final TextView textView2 = binding.eventDate;
        textView2.setText("Deadline: "+ Converting_Timestamp_to_String(selectedEvent.getDate()));

        // Sets the Organizers name
        final TextView textView3 = binding.organizerName;
        textView3.setText("Organizer: "+ selectedEvent.getOrganizerID());
        // String List[states] = new List[]{"Registered", "Chosen", "Cancelled", "Signups"};

        // Getting number of entrants when loading fragment
        final int eventLimit = selectedEvent.getEventSize();
        final int entrants = selectedEvent.getSignUps().toArray().length;
        final TextView textView4 = binding.eventSpots;
        textView4.setText("Entrants: "+entrants+"/"+eventLimit);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null){
            isSignedUp = savedInstanceState.getBoolean("isSignedUp", false);
            isWaiting = isSignedUp;
        }

        // Checks if post draw conditions are met, people already drew for lottery
        if(!selectedEvent.getChosen().isEmpty()){
            postDraw = true;
        }


        //logState("render");
        updateSignUpButton(isChosen,isAttendee,isWaiting,isCancelled, isSignedUp);

        binding.btnSignUp.setOnClickListener(v -> {
            if (!postDraw) {
                // BEFORE lottery
                isSignedUp = !isSignedUp;
            }
            else {
                // AFTER lottery:
                if (isChosen) {
                    // When chosen, user can select to be an attendee or cancel
                    showTwoButton();
                    return;
                }
                else {
                    if(!isSignedUp && !isCancelled){
                        isWaiting = !isWaiting;
                    }
                }
            }
            //logState("render");
            updateSignUpButton(isChosen, isAttendee, isWaiting, isCancelled, isSignedUp);
        });

        // Accept chosen spot
        binding.btnAccept.setOnClickListener(v -> {
            isAttendee = true;
            isChosen = false;
            isWaiting = false;
            isCancelled = false;
            isSignedUp = false;
            updateSignUpButton(isChosen, isAttendee, isWaiting, isCancelled, isSignedUp);
        });

        // Decline chosen spot
        binding.btnDecline.setOnClickListener(v -> {
            isAttendee = false;
            isChosen = false;
            isWaiting = false;
            isCancelled = true;
            isSignedUp = false;
            updateSignUpButton(isChosen, isAttendee, isWaiting, isCancelled, isSignedUp);
        });

        // Displays Term of Services
        binding.btnHelp.setOnClickListener(v -> showTermsDialog());

        // Goes Back to home
        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );
    }

    private void updateSignUpButton(boolean isChosen, boolean isRegistered, boolean isWaiting,
                                    boolean isCancelled, boolean isSignedUp){

        // Before lottery
        if(!postDraw) {
            showSingleButton(isSignedUp ? "Unregister" : "Register");
            return;
            }
        // after the lottery
        if(isCancelled){
            showMessage("Maybe Next time...");
            return;
            }

        if(isRegistered){
            showMessage("See you soon!");
            return;
        }

        // Accept/Decline in the same bottom slot
        if (isChosen) {
            showTwoButton();

            return;
        }

        showSingleButton(isWaiting ? "Leave Waitlist" : "Join Waitlist");
    }

    private void showTermsDialog(){
        new AlertDialog.Builder(requireContext())
                .setTitle("Terms of Service")
                .setMessage("1) Be kind.\n2) Follow organizer rules.\n3) Respect the venue." +
                        "\n\nBy continuing, you agree to these terms.")
                .setPositiveButton("OK", (d,w)->d.dismiss())
                .show();
    }

    private void showSingleButton(String text){
        binding.btnSignUp.setText(text);
        binding.btnSignUp.setEnabled(true);

        binding.btnSignUp.setVisibility(View.VISIBLE);
        binding.lotteryResponse.setVisibility(View.GONE);
        binding.bottomMessage.setVisibility(View.GONE);
    }

    private void showTwoButton(){
        binding.btnSignUp.setVisibility(View.GONE);
        binding.lotteryResponse.setVisibility(View.VISIBLE);
        binding.bottomMessage.setVisibility(View.GONE);
    }

    private void showMessage(String text) {
        binding.bottomMessage.setText(text);
        binding.bottomMessage.setVisibility(View.VISIBLE);

        binding.btnSignUp.setVisibility(View.GONE);
        binding.lotteryResponse.setVisibility(View.GONE);
    }

    private String Converting_Timestamp_to_String(@Nullable com.google.firebase.Timestamp ts) {
        if (ts == null) return "";
        java.util.Date d = ts.toDate();
        java.text.DateFormat df = android.text.format.DateFormat.getMediumDateFormat(requireContext());
        java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(requireContext());
        return df.format(d) + " " + tf.format(d);
    }

    private void logState(String tag) {
        android.util.Log.d("EventDetail", tag + " postDraw=" + postDraw
                + " chosen=" + isChosen
                + " attendee=" + isAttendee
                + " cancelled=" + isCancelled
                + " waiting=" + isWaiting);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
