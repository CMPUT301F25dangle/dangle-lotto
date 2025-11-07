package com.example.dangle_lotto.ui.detail;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentEventDetailBinding;
import com.google.firebase.Timestamp;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Displays the full details of an event and handles user interaction
 * depending on their event participation state.
 *
 * User States:
 *  - Registered: on the waiting list before draw
 *  - Chosen: selected from the lottery
 *  - SignUp: user confirmed attendance
 *  - Cancelled: user declined after being chosen
 */
public class EventDetailFragment extends Fragment {

    private FragmentEventDetailBinding binding;
    private FirebaseManager firebaseManager;
    private UserViewModel userViewModel;
    private Event selectedEvent;

    // Four main user state flags
    private boolean isRegistered = false;
    private boolean isChosen = false;
    private boolean isSignedUp = false;
    private boolean isCancelled = false;
    private boolean postDraw = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseManager = new FirebaseManager();
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        selectedEvent = userViewModel.getSelectedHomeEvent().getValue();

        if (selectedEvent == null) {
            Log.e("EventDetailFragment", "No selected event found.");
            return root;
        }

        // Display event information
        binding.eventTitle.setText(selectedEvent.getName());
        binding.eventDescription.setText(selectedEvent.getDescription());
        binding.eventDate.setText("Deadline: " + formatTimestamp(selectedEvent.getDate()));
        binding.organizerName.setText("Organizer: " + selectedEvent.getOrganizerID());

        int eventLimit = selectedEvent.getEventSize();
        int chosenCount = selectedEvent.getChosen().size();
        if (eventLimit > 0) {
            int spotsRemaining = Math.max(0, eventLimit - chosenCount);
            binding.eventSpots.setText("Spots Remaining: " + spotsRemaining + "/" + eventLimit);
        } else {
            binding.eventSpots.setText("Attendees: " + chosenCount);
        }

        binding.btnBack.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final User currentUser = userViewModel.getUser().getValue();
        if (selectedEvent == null || currentUser == null) return;
        final String uid = currentUser.getUid();

        // Restore from rotation if needed
        if (savedInstanceState != null) {
            isRegistered = savedInstanceState.getBoolean("isRegistered", false);
            isChosen = savedInstanceState.getBoolean("isChosen", false);
            isSignedUp = savedInstanceState.getBoolean("isSignedUp", false);
            isCancelled = savedInstanceState.getBoolean("isCancelled", false);
        }

        // Determine if lottery already drawn
        postDraw = !selectedEvent.getChosen().isEmpty();

        // Sync user state from event lists
        isRegistered = selectedEvent.getRegistered().contains(uid);
        isChosen = selectedEvent.getChosen().contains(uid);
        isSignedUp = selectedEvent.getSignUps().contains(uid);
        isCancelled = selectedEvent.getCancelled().contains(uid);

        updateButtonState();

        binding.btnSignUp.setOnClickListener(v -> handleClick(uid));
    }

    /**
     * Handle button click depending on user’s event state.
     */
    private void handleClick(String uid) {
        if (!postDraw) {
            // BEFORE LOTTERY — join or leave the registration list
            if (isRegistered) {
                performTask(selectedEvent.deleteRegistered(uid), "Registration removed");
                isRegistered = false;
            } else {
                performTask(selectedEvent.addRegistered(uid), "Registered for lottery");
                isRegistered = true;
            }
        } else {
            // AFTER LOTTERY
            if (isChosen && !isSignedUp && !isCancelled) {
                // User was chosen — ask to confirm or cancel
                showChosenDialog(uid);
                return;
            }

            // If not chosen, allow waitlist toggling
            if (!isChosen && !isSignedUp && !isCancelled) {
                if (isRegistered) {
                    performTask(selectedEvent.deleteRegistered(uid), "Left waitlist");
                    isRegistered = false;
                } else {
                    performTask(selectedEvent.addRegistered(uid), "Joined waitlist");
                    isRegistered = true;
                }
            }
        }

        updateButtonState();
    }

    /**
     * Displays dialog for chosen users to accept or decline.
     */
    private void showChosenDialog(String uid) {
        new AlertDialog.Builder(requireContext())
                .setTitle("You’ve Been Chosen!")
                .setMessage("Would you like to attend this event?")
                .setPositiveButton("Attend", (dialog, which) -> {
                    performTask(selectedEvent.addSignUp(uid), "Confirmed attendance");
                    isSignedUp = true;
                    isCancelled = false;
                    updateButtonState();
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    performTask(selectedEvent.addCancelled(uid), "Declined invitation");
                    isCancelled = true;
                    isSignedUp = false;
                    updateButtonState();
                })
                .show();
    }

    /**
     * Updates the button text and state depending on user’s position in the event flow.
     */
    private void updateButtonState() {
        Button btn = binding.btnSignUp;

        if (isCancelled) {
            btn.setText("Cancelled");
            btn.setEnabled(false);
        } else if (isSignedUp) {
            btn.setText("Attending");
            btn.setEnabled(false);
        } else if (isChosen) {
            btn.setText("You’ve Been Chosen!");
            btn.setEnabled(true);
        } else if (postDraw) {
            btn.setText(isRegistered ? "Leave Waitlist" : "Join Waitlist");
            btn.setEnabled(true);
        } else {
            btn.setText(isRegistered ? "Withdraw Registration" : "Register for Lottery");
            btn.setEnabled(true);
        }
    }

    /**
     * Executes a Firebase Task safely with toast feedback.
     */
    private void performTask(Task<Void> task, String successMsg) {
        binding.btnSignUp.setEnabled(false);
        task.addOnCompleteListener(t -> {
            binding.btnSignUp.setEnabled(true);
            if (t.isSuccessful()) {
                showMessage(successMsg);
            } else {
                showMessage("Error: " + (t.getException() != null ? t.getException().getMessage() : "Unknown"));
            }
        });
    }

    private void showMessage(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) return "N/A";
        Date date = ts.toDate();
        DateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return df.format(date);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRegistered", isRegistered);
        outState.putBoolean("isChosen", isChosen);
        outState.putBoolean("isSignedUp", isSignedUp);
        outState.putBoolean("isCancelled", isCancelled);
    }
}
