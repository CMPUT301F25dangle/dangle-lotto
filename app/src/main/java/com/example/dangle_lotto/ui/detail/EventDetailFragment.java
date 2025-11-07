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
import com.example.dangle_lotto.User;
import com.google.firebase.Timestamp;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentEventDetailBinding;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Event Detail - Gives necessary information about an event
 * such as Organizer name, Deadline to signup, event description, event poster
 * , spots available
 *
 * @author Cainan
 * @version 1.0
 * @since 2025-11-07
 */

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


    /**
     *
     * Inflate and puts necessary information on startup of fragment view
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
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
        textView2.setText("Deadline: "+ formatTimestamp(selectedEvent.getDate()));

        // Sets the Organizers name
        final TextView textView3 = binding.organizerName;
        textView3.setText("Organizer: "+ selectedEvent.getOrganizerID());
        // String List[states] = new List[]{"Registered", "Chosen", "Cancelled", "Signups"};

        // Getting number of entrants when loading fragment
        final int eventLimit = selectedEvent.getEventSize();
        final int spotsRemaining = selectedEvent.getChosen().size();
        final TextView textView4 = binding.eventSpots;
        if(eventLimit > 0){
            textView4.setText("Spots Remaining: "+spotsRemaining+"/"+eventLimit);
        }
        else {
            textView4.setText("Attendees: "+spotsRemaining);
        }

        return root;
    }

    /**
     *
     * Handles the interactive wiring and live updates
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final User currentUser = userViewModel.getUser().getValue();
        final int eventLimit = selectedEvent.getEventSize();
        final int entrants = selectedEvent.getSignUps().size();
        final TextView textView4 = binding.eventSpots;



        if(savedInstanceState != null){
            isSignedUp = savedInstanceState.getBoolean("isSignedUp", false);
            isWaiting = isSignedUp;
        }

        // Checks if post draw conditions are met, people already drew for lottery
        postDraw = !selectedEvent.getChosen().isEmpty();
        isSignedUp = !selectedEvent.getSignUps().isEmpty();

        //logState("render");
        updateSignUpButton(isChosen,isAttendee,isWaiting,isCancelled, isSignedUp);

        binding.btnSignUp.setOnClickListener(v -> {

            if (currentUser == null) { showMessage("Please sign in first."); return; }

            if (!postDraw) {
                // BEFORE lottery: toggle SignUps
                isSignedUp = !isSignedUp;
                if (isSignedUp) {
                    safeAddSignUp(currentUser);
                } else {
                    safeRemoveSignUp(currentUser);
                }
            } else {
                // AFTER lottery
                if (isChosen) { showTwoButton(); return; }

                if (!isAttendee && !isCancelled) {
                    isWaiting = !isWaiting;
                    if (isWaiting) {
                        safeAddRegistered(currentUser);   // join waitlist
                    } else {
                        safeRemoveRegistered(currentUser); // leave waitlist
                    }
                }
            }

            updateSignUpButton(isChosen, isAttendee, isWaiting, isCancelled, isSignedUp);
        });


        // Accept chosen spot
        binding.btnAccept.setOnClickListener(v -> {

            if (currentUser == null) { showMessage("Please sign in first."); return; }

            isAttendee = true; isChosen = false; isWaiting = false; isCancelled = false; isSignedUp = false;
            // Move them to REGISTERED definitively
            safeAddRegistered(currentUser);
            updateSignUpButton(isChosen, isAttendee, isWaiting, isCancelled, isSignedUp);
            // After accepting the lottery offer it will update
            final int updated_chosen = selectedEvent.getChosen().size();
            if(eventLimit > 0){
                textView4.setText("Spots Remaining: "+updated_chosen+"/"+eventLimit);
            }
            else {
                textView4.setText("Attendees: "+entrants);
            }
        });

        // Decline chosen spot
        binding.btnDecline.setOnClickListener(v -> {

            if (currentUser == null) { showMessage("Please sign in first."); return; }

            isAttendee = false; isChosen = false; isWaiting = false; isCancelled = true; isSignedUp = false;

            safeRemoveChosen(currentUser);
            safeAddCancelled(currentUser);

            updateSignUpButton(isChosen, isAttendee, isWaiting, isCancelled, isSignedUp);

        });

        // Displays Term of Services
        binding.btnHelp.setOnClickListener(v -> showTermsDialog());

        // Goes Back to home
        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );
    }

    /**
     *
     * Updates Signup Button based on states: if need to sign up for event, be put on the waiting
     * list, if entrant is chosen by lottery -> if accept/decline offer
     *
     *
     * @param isChosen
     * @param isRegistered
     * @param isWaiting
     * @param isCancelled
     * @param isSignedUp
     */
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

    /**
     * Displays terms of Service
     */
    private void showTermsDialog(){
        new AlertDialog.Builder(requireContext())
                .setTitle("Terms of Service")
                .setMessage("1) Be kind.\n2) Follow organizer rules.\n3) Respect the venue." +
                        "\n\nBy continuing, you agree to these terms.")
                .setPositiveButton("OK", (d,w)->d.dismiss())
                .show();
    }

    /**
     * Makes the single button visible to screen, and takes an input to put on button
     * @param text
     */

    private void showSingleButton(String text){
        binding.btnSignUp.setText(text);
        binding.btnSignUp.setEnabled(true);

        binding.btnSignUp.setVisibility(View.VISIBLE);
        binding.lotteryResponse.setVisibility(View.GONE);
        binding.bottomMessage.setVisibility(View.GONE);
    }

    /**
     * Makes the two button accept and decline visible to the screen
     */
    private void showTwoButton(){
        binding.btnSignUp.setVisibility(View.GONE);
        binding.lotteryResponse.setVisibility(View.VISIBLE);
        binding.bottomMessage.setVisibility(View.GONE);
    }

    /**
     * Shows message
     * @param text
     */

    private void showMessage(String text) {
        binding.bottomMessage.setText(text);
        binding.bottomMessage.setVisibility(View.VISIBLE);

        binding.btnSignUp.setVisibility(View.GONE);
        binding.lotteryResponse.setVisibility(View.GONE);
    }

    /**
     * Converts firebase timestamp to string
     * @param ts
     * @return
     */

    private String formatTimestamp(@Nullable com.google.firebase.Timestamp ts) {
        if (ts == null) return "";
        java.util.Date d = ts.toDate();
        java.text.DateFormat df = android.text.format.DateFormat.getMediumDateFormat(requireContext());
        java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(requireContext());
        return df.format(d) + " " + tf.format(d);
    }


    /**
     * Gets user id from user class
     * @param u
     * @return
     */
    private String uid(User u){ return u.getUid(); }

    /**
     * Helper function to get add status from firebase
     * @param u
     * @param list
     */
    private void friendlyAdd(User u, String list) {
        firebaseManager.userAddStatus(u.getUid(), selectedEvent.getEid(), list);
    }

    /**
     * Helper function to get removal status from firebase
     * @param u
     * @param list
     */

    private void friendlyRemoval(User u, String list)  {
        firebaseManager.userRemoveStatus(u.getUid(), selectedEvent.getEid(), list);
    }

    /**
     * Safely Adds user to sign up
     * @param u
     */

    private void safeAddSignUp(User u) {
        String id = uid(u);
        if (!selectedEvent.getSignUps().contains(id)) {
            // Ensure exclusivity, but safely
            selectedEvent.getSignUps().add(id);
            friendlyAdd(u, "SignUps");
        }
        // make sure user is not in other lists
        selectedEvent.getRegistered().remove(id); friendlyRemoval(u, "Register");
        selectedEvent.getChosen().remove(id);     friendlyRemoval(u, "Chosen");
        selectedEvent.getCancelled().remove(id);  friendlyRemoval(u, "Cancelled");
    }

    /**
     * Safely removes User from sign up
     * @param u
     */

    private void safeRemoveSignUp(User u) {
        String id = uid(u);
        if (selectedEvent.getSignUps().remove(id)) {
            friendlyRemoval(u, "SignUps");
        }
    }

    /**
     * Safely add user to registered while removing them from other lists
     * @param u
     */

    private void safeAddRegistered(User u) {
        String id = uid(u);
        if (!selectedEvent.getRegistered().contains(id)) {
            selectedEvent.getRegistered().add(id);
            friendlyAdd(u, "Register");
        }
        // remove from lists that must be exclusive
        selectedEvent.getSignUps().remove(id); friendlyRemoval(u, "SignUps");
        selectedEvent.getChosen().remove(id);  friendlyRemoval(u, "Chosen");
        selectedEvent.getCancelled().remove(id); friendlyRemoval(u, "Cancelled");
    }

    /**
     * Safely removes user from registered list
     * @param u
     */

    private void safeRemoveRegistered(User u) {
        String id = uid(u);
        if (selectedEvent.getRegistered().remove(id)) {
            friendlyRemoval(u, "Register");
        }
    }

    /**
     *  Safely adds user to cancelled list
     * @param u
     */

    private void safeAddCancelled(User u) {
        String id = uid(u);
        if (!selectedEvent.getCancelled().contains(id)) {
            selectedEvent.getCancelled().add(id);
            friendlyAdd(u, "Cancelled");
        }
        // remove from others
        selectedEvent.getRegistered().remove(id); friendlyRemoval(u, "Register");
        selectedEvent.getChosen().remove(id);     friendlyRemoval(u, "Chosen");
        selectedEvent.getSignUps().remove(id);    friendlyRemoval(u, "SignUps");
    }

    /**
     * Safely removes user from chosen
     * @param u
     */

    private void safeRemoveChosen(User u) {
        String id = uid(u);
        if (selectedEvent.getChosen().remove(id)) {
            friendlyRemoval(u, "Chosen");
        }
    }
    /**
     * Destroys view
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
