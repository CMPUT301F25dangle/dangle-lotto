package com.example.dangle_lotto.ui.dashboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentOrganizerEventDetailsEventBinding;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * OrganizerEventDetailsEventFragment - Fragment shows event details of an event.
 * <p>
 * This fragment opens inside of the OrganizerEventDetails fragment.
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 2025-11-05
 */
public class OrganizerEventDetailsEventFragment extends Fragment {
    private FragmentOrganizerEventDetailsEventBinding binding;
    private UserViewModel userViewModel;
    private Event event;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOrganizerEventDetailsEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        event = userViewModel.getSelectedOrganizedEvent().getValue();

        // Display event information
        binding.organizerEventDetailsEventDescription.setText(event.getDescription());
        binding.organizerEventDetailsEventStartDate.setText("Opens: " + formatTimestamp(event.getStartDate()));
        binding.organizerEventDetailsEventEndDate.setText("Closes: " + formatTimestamp(event.getEndDate()));
        binding.organizerEventDetailsEventEventDate.setText("Event Date: " + formatTimestamp(event.getEventDate()));

        // Display event poster (if available")
        if (!(event.getPhotoID().isEmpty() || event.getPhotoID() == null))
            Glide.with(requireContext()).load(event.getPhotoID()).into(binding.organizerEventDetailsEventPoster);

        // Display categories if available
        if (!event.getCategories().isEmpty()) {
            binding.organizerEventDetailsEventCategoriesInput.setText(String.join(", ", event.getCategories()));
        } else {
            binding.organizerEventDetailsEventCategoriesInput.setText("None");
        }

        // Geolocation

        // Display spots remaining
        updateSpotsUI();

        // Update spots remaining when spots change

        return root;
    }

    /**
     * Updates the number of spots remaining in the event.
     */
    private void updateSpotsUI() {
        int registrantsLimit = event.getMaxEntrants();
        int registrantsCount = event.getSignUps().size() + event.getCancelled().size() + event.getChosen().size() + event.getRegistered().size();

        if (registrantsLimit != -1) {
            int spotsRemaining = Math.max(0, registrantsLimit - registrantsCount);
            binding.organizerEventDetailsEventSpots.setText("Spots Remaining: " + spotsRemaining + "/" + registrantsLimit);
        } else {
            binding.organizerEventDetailsEventSpots.setText("Registrants: " + registrantsCount);
        }
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
}