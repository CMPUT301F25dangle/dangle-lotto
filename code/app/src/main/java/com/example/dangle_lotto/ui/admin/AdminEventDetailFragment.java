package com.example.dangle_lotto.ui.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.databinding.FragmentAdminEventDetailBinding;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        binding = FragmentAdminEventDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        adminViewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        selectedEvent = adminViewModel.getSelectedEvent().getValue();

        if (selectedEvent == null) {
            Log.e("EventDetaulFragment", "Noe selected event found.");
            return root;
        }

        binding.adminEventTitle.setText(selectedEvent.getName());
        binding.adminEventDescription.setText(selectedEvent.getDescription());
        binding.adminEventDate.setText("Registration Period: " + formatTimeStamp(selectedEvent.getStartDate()) + " to " + formatTimeStamp(selectedEvent.getEndDate()));
        if (!(selectedEvent.getPhotoID().isEmpty()) || selectedEvent.getPhotoID() == null) {
            Glide.with(requireContext()).load(selectedEvent.getPhotoID()).into(binding.adminImgPoster);
        }
        updateSpotsUI();

        binding.adminBtnBack.setOnClickListener(v -> {
            adminViewModel.setSelectedEvent(null);
            Navigation.findNavController(v).popBackStack();
        });

        binding.adminEventDetailInformationButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext()).setTitle("Event Criteria").setMessage("Everybody is able to register for this event. To register, simply click the 'Register for Lottery' button. The lottery will randomly select registrants, who will be given the option to sign up for the event. If some chosen users decline, then more registrants will be randomly chosen.\nThe maximum size of event: " + selectedEvent.getMaxEntrants())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        binding.adminDeleteEventButton.setOnClickListener(v->{
            Bundle result = new Bundle();
            result.putString("deleted", adminViewModel.getSelectedEvent().getValue().getEid());
            getParentFragmentManager().setFragmentResult("eventDeleted", result);
            firebaseManager.deleteEvent(selectedEvent.getEid());
            Log.d("Admin delete event", selectedEvent.getEid());
            adminViewModel.setSelectedEvent(null);
            // TODO: send a notification
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
     * format time
     *
     * @param ts
     * @return String
     */
    private String formatTimeStamp(Timestamp ts) {
        if (ts == null) return "N/A";
        Date date = ts.toDate();
        DateFormat df = new SimpleDateFormat("MM dd, yyyy HH:mm", Locale.getDefault());
        return df.format(date);
    }

}