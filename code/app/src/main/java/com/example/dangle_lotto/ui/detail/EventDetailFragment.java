package com.example.dangle_lotto.ui.detail;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.GeneralUser;
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
 * <p>
 * User States:
 *  - Registered: on the waiting list before draw
 *  - Chosen: selected from the lottery
 *  - SignUp: user confirmed attendance
 *  - Cancelled: user declined after being chosen
 */
public class EventDetailFragment extends Fragment {

    private FragmentEventDetailBinding binding;
    private UserViewModel userViewModel;
    private Event selectedEvent;

    // Four main user state flags
    private boolean isRegistered = false;
    private boolean isChosen = false;
    private boolean isSignedUp = false;
    private boolean isCancelled = false;
    private boolean postDraw = false;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        selectedEvent = userViewModel.getSelectedEvent().getValue();

        if (selectedEvent == null) {
            Log.e("EventDetailFragment", "No selected event found.");
            return root;
        }

        // get organizer and set their name
        firebaseManager.getUser(selectedEvent.getOrganizerID(), new FirebaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                binding.organizerName.setText("Organizer: " + result.getUsername());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Display event information
        binding.eventTitle.setText(selectedEvent.getName());
        binding.eventDescription.setText(selectedEvent.getDescription());
        binding.eventDate.setText("Registration Period: " + formatTimestamp(selectedEvent.getStartDate())
                + " to " + formatTimestamp(selectedEvent.getEndDate()));
        if (!(selectedEvent.getPhotoID().isEmpty() || selectedEvent.getPhotoID() == null))
            Glide.with(requireContext()).load(selectedEvent.getPhotoID()).into(binding.imgPoster);

        updateSpotsUI();

        binding.btnBack.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        binding.eventDetailInformationButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Event Criteria")
                    .setMessage("Everybody is able to register for this event. To register, simply click the 'Register for Lottery' button. The lottery will randomly select registrants, who will be given the option to sign up for the event. If some chosen users decline, then more registrants will be randomly chosen.\nThe maximum size of event: " + selectedEvent.getMaxEntrants())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });

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

        binding.eventDetailDynamicButton.setOnClickListener(v -> handleClick(uid));
    }

    /**
     * Updates the number of spots remaining in the event.
     */
    private void updateSpotsUI() {
        Integer registrantsLimit = selectedEvent.getMaxEntrants();
        int registrantsCount = selectedEvent.getSignUps().size() + selectedEvent.getCancelled().size() + selectedEvent.getChosen().size() + selectedEvent.getRegistered().size();

        if (registrantsLimit != -1) {
            int spotsRemaining = Math.max(0, registrantsLimit - registrantsCount);
            binding.eventSpots.setText("Spots Remaining: " + spotsRemaining + "/" + registrantsLimit);
        } else {
            binding.eventSpots.setText("Registrants: " + registrantsCount);
        }
    }

    /**
     * Handle button click depending on user’s event state.
     *
     * @param uid User ID of the clicked user.
     */
    private void handleClick(String uid) {
        if (!postDraw) {
            // BEFORE LOTTERY — join or leave the registration list
            if (isRegistered) {
                performTask(selectedEvent.deleteRegistered(uid));
                isRegistered = false;
            } else {
                performTask(selectedEvent.addRegistered(uid));
                isRegistered = true;
            }

            updateSpotsUI();

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
                    performTask(selectedEvent.deleteRegistered(uid));
                    isRegistered = false;
                } else {
                    performTask(selectedEvent.addRegistered(uid));
                    isRegistered = true;
                }

                updateSpotsUI();

            }
        }
    }

    /**
     * Displays dialog for chosen users to accept or decline.
     *
     * @param uid User ID of the chosen user.
     */
    private void showChosenDialog(String uid) {
        new AlertDialog.Builder(requireContext())
                .setTitle("You’ve Been Chosen!")
                .setMessage("Would you like to attend this event?")
                .setPositiveButton("Attend", (dialog, which) -> {
                    performTask(selectedEvent.addSignUp(uid));
                    isSignedUp = true;
                    isCancelled = false;
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    performTask(selectedEvent.addCancelled(uid));
                    isCancelled = true;
                    isSignedUp = false;
                })
                .show();
    }

    /**
     * Updates the button text and state depending on user’s position in the event flow.
     */
    private void updateButtonState() {
        Button btn = binding.eventDetailDynamicButton;

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
     *
     * @param task Firebase Task to execute.
     */
    private void performTask(Task<Void> task) {
        binding.eventDetailDynamicButton.setEnabled(false);
        task.addOnCompleteListener(t -> {
            binding.eventDetailDynamicButton.setEnabled(true);
            if (t.isSuccessful()) {
                updateButtonState();
            }
        });
    }

    /**
     * Formats a Firebase Timestamp for display.
     *
     * @param ts Timestamp to format.
     * @return Formatted date string.
     */
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
