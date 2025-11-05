package com.example.dangle_lotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentHomeBinding;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

/**
 * HomeFragment - Fragment shows events that user can click for more info. Allows allows user to open a filter.
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 2025-10-25
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private ArrayList<String> selectedFilters = new ArrayList<>();
    private FirebaseManager firebaseManager = new FirebaseManager();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.homeEventRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<Event> events = new ArrayList<>();

        // temporarily adding for testing
        for (int i = 1; i < 4; i++) {
            events.add(firebaseManager.createEvent("bruh", "bruh", Timestamp.now(), "bruh", "bruh", 10, "bruh"));
        }
        EventCardAdapter adapter = new EventCardAdapter(events, position -> {
            Event event = events.get(position);
            openEventFragment();
        });
        recyclerView.setAdapter(adapter);

        // initialize button for opening filter dialogue
        binding.filterButton.setOnClickListener(v -> openFilterDialogue());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Opens the event that is requested using nav controller
     */
    private void openEventFragment() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_navigation_home_to_placeholderFragment);
    }

    /**
     * Opens the filter dialogue
     *
     * <p>Sets filters in the dialogue, and apply listener</p>
     */
    private void openFilterDialogue() {
        FilterDialogueFragment dialog = new FilterDialogueFragment();

        // set the selected filter on the dialogue
        dialog.setPreselectedFilters(selectedFilters);

        // set listener on dialogue to use filters
        dialog.setOnFilterSelectedListener(filters -> {
            selectedFilters = new ArrayList<>(filters);

            // update UI based on filters
        });

        dialog.show(getParentFragmentManager(), "FilterDialog");
    }
}