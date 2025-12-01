package com.example.dangle_lotto.ui.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.databinding.FragmentAdminEventDetailBinding;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * AdmenEventDetailFragment - this fragment is displayed when an admin selects an event
 * Admins can view the details and delete the event.
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-27
 */

public class AdminEventDetailFragment extends Fragment {
    private FragmentAdminEventDetailBinding binding;
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private AdminViewModel adminViewModel;
    private Event selectedEvent;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentAdminEventDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize ViewModel
        adminViewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        selectedEvent = adminViewModel.getSelectedEvent().getValue();

        // get organizer and set their name
        firebaseManager.getUser(selectedEvent.getOrganizerID(), new FirebaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                binding.adminOrganizerName.setText("Organizer: " + result.getUsername());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Set event details
        binding.adminEventTitle.setText(selectedEvent.getName());
        binding.adminEventDescription.setText(selectedEvent.getDescription());
        binding.adminEventStartDate.setText("Opens: " + formatTimeStamp(selectedEvent.getStartDate()));
        binding.adminEventEndDate.setText("Closes: " + formatTimeStamp(selectedEvent.getEndDate()));
        binding.adminEventEventDate.setText("Event Date: " + formatTimeStamp(selectedEvent.getEndDate()));

        // Display event poster (if available")
        if (!(selectedEvent.getPhotoID().isEmpty() || selectedEvent.getPhotoID() == null))
            Glide.with(requireContext()).load(selectedEvent.getPhotoID()).into(binding.adminImgPoster);

        // Display categories if available
        if (!selectedEvent.getCategories().isEmpty()) {
            binding.adminEventDetailsCategoriesInput.setText(String.join(", ", selectedEvent.getCategories()));
        } else {
            binding.adminEventDetailsCategoriesTitle.setVisibility(View.GONE);
            binding.adminEventDetailsCategoriesInput.setVisibility(View.GONE);
        }

        // Geolocation
        if (selectedEvent.isLocationRequired()) {
            binding.adminEventDetailGeolocation.setText("Geolocation Is Required For This Event");
        }

        updateSpotsUI();

        // Set back button
        binding.adminBtnBack.setOnClickListener(v -> {
            adminViewModel.setSelectedEvent(null);
            Navigation.findNavController(v).popBackStack();
        });

        // Display event criteria
        binding.adminEventDetailInformationButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Event Criteria")
                    .setMessage("Everybody is able to register for this event. To register, simply click the 'Register for Lottery' button. The lottery will randomly select registrants, who will be given the option to sign up for the event. If some chosen users decline, then more registrants will be randomly chosen.\nThe maximum size of event: " + selectedEvent.getMaxEntrants())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Set delete event button
        binding.adminDeleteEventButton.setOnClickListener(v -> {
            // Delete event from database
            firebaseManager.deleteEvent(selectedEvent.getEid());
            Log.d("Admin delete event", selectedEvent.getEid());

            // Remove event from view model
            ArrayList<Event> events = adminViewModel.getEvents().getValue();
            String removeId = selectedEvent.getEid();
            events.removeIf(e -> e.getEid().equals(removeId));
            adminViewModel.setEvents(events);

            firebaseManager.createNotification(adminViewModel.getUser().getValue(), selectedEvent.getOrganizerID(), "Deleted event " + selectedEvent.getName(), true);
            // Return to previous fragment
            Navigation.findNavController(v).popBackStack();
        });

        return root;
    }


    /**
     * Updates the number of spots remaining in the event.
     */
    private void updateSpotsUI() {
        Integer registrantsLimit = selectedEvent.getMaxEntrants();
        int registrantsCount = selectedEvent.getSignUps().size() + selectedEvent.getCancelled().size() + selectedEvent.getChosen().size() + selectedEvent.getRegistered().size();

        if (registrantsLimit != -1) {
            int spotsRemaining = Math.max(0, registrantsLimit - registrantsCount);
            binding.adminEventSpots.setText("Spots Remaining: " + spotsRemaining + "/" + registrantsLimit);
        } else {
            binding.adminEventSpots.setText("Registrants: " + registrantsCount);
        }
    }


    /**
     * Formats the time
     *
     * @param ts Timestamp
     * @return String
     */
    private String formatTimeStamp(Timestamp ts) {
        if (ts == null) return "N/A";
        Date date = ts.toDate();
        DateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return df.format(date);
    }

}