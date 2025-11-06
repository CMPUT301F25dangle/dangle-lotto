package com.example.dangle_lotto.ui.dashboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentOrganizerEventDetailsBinding;

/**
 * This fragment creates the organizer view of their event
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 2025-11-04
 */
public class OrganizerEventDetailsFragment extends Fragment {
    private FragmentOrganizerEventDetailsBinding binding;
    private Button eventButton;
    private Button entrantsButton;
    private Button mapButton;
    private Button[] buttons;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerEventDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // making back button actually take u to previous fragment
        binding.organizerEventDetailsBackButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        // initialize buttons
        eventButton = binding.organizerEventDetailsEventButton;
        entrantsButton = binding.organizerEventDetailsEntrantsButton;
        mapButton = binding.organizerEventDetailsMapButton;
        buttons = new Button[]{eventButton, entrantsButton, mapButton};

        // set listeners for buttons
        setupButtonListeners();

        // default selection & fragment
        eventButton.setSelected(true);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.organizer_event_view_display, new OrganizerEventDetailsEventFragment())
                .commit();

        return root;
    }

    /**
     * Sets up the listeners for the buttons
     */
    private void setupButtonListeners() {
        for (Button button : buttons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTabButtonClicked((Button) v);
                }
            });
        }
    }

    /**
     * Called when a button is clicked
     * </p>
     * Deselects other buttons and keeps the clicked button selected
     *
     * @param clickedButton Button that as clicked
     */
    private void onTabButtonClicked(Button clickedButton) {
        // deselect all buttons
        for (Button b : buttons) {
            b.setSelected(false);
        }
        // select clicked button
        clickedButton.setSelected(true);

        Fragment fragment = null;

        if (clickedButton == eventButton) {
            fragment = new OrganizerEventDetailsEventFragment();
        } else if (clickedButton == entrantsButton) {
            fragment = new OrganizerEventDetailsEntrantsFragment();
        } else if (clickedButton == mapButton) {
            fragment = new OrganizerEventDetailsMapFragment();
        }

        if (fragment != null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.organizer_event_view_display, fragment)
                    .commit();
        }
    }
}
