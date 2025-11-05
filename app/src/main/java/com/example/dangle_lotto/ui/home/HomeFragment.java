package com.example.dangle_lotto.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.FirestoreCallback;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentHomeBinding;

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
    private ArrayList<Event> events;
    private EventCardAdapter adapter;
    private LinearLayoutManager layoutManager;
    private boolean isLoading;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initializing recycler view
        recyclerView = binding.homeEventRecyclerView;
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // initializing events list
        events = new ArrayList<>();

        // temporarily adding for testing
//        for (int i = 1; i < 4; i++) {
//            events.add(firebaseManager.createEvent("bruh", "bruh", Timestamp.now(), "bruh", "bruh", 10, "bruh"));
//        }

        // initializing and attaching adapter
         adapter = new EventCardAdapter(events, position -> {
            Event event = events.get(position);
            openEventFragment();
        });
        recyclerView.setAdapter(adapter);

        // detects when user reaches end
        isLoading = false;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // check if were near the end
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                        && firstVisibleItemPosition >= 0) {
                    loadNextPage();
                }
            }
        });

        firebaseManager.getEvent("7rHZzxyUqLwcju31Rxe8", new FirestoreCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                events.add(event);
                adapter.notifyItemInserted(0);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firebase", "Failed to load event", e);
            }
        });

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
        dialog.setPreselectedCategories(selectedFilters);

        // set listener on dialogue to use filters
        dialog.setOnFilterSelectedListener(filters -> {
            selectedFilters = new ArrayList<>(filters);

            // update UI based on filters
        });

        dialog.show(getParentFragmentManager(), "FilterDialog");
    }

    private void loadNextPage() {
        Toast.makeText(getContext(), "At the end of the list!", Toast.LENGTH_SHORT).show();
//        if (isLoading) return;
//        isLoading = true;
//
//        Query query = firebaseManager.getEventsQuery()
//                .startAfter(lastVisible)
//                .limit(PAGE_SIZE);
//
//        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
//            if (!queryDocumentSnapshots.isEmpty()) {
//                lastVisible = queryDocumentSnapshots.getDocuments()
//                        .get(queryDocumentSnapshots.size() - 1);
//                for (DocumentSnapshot doc : queryDocumentSnapshots) {
//                    events.add(new Event(...)); // parse doc
//                }
//                adapter.notifyDataSetChanged();
//            }
//            isLoading = false;
//        });
    }

}