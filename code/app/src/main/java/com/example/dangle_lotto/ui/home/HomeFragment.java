package com.example.dangle_lotto.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentHomeBinding;
import com.example.dangle_lotto.ui.EventCardAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;

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
    private FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private UserViewModel userViewModel;
    private ArrayList<Event> events;
    private EventCardAdapter adapter;
    private LinearLayoutManager layoutManager;
    private boolean isLoading;
    private static final int PAGE_SIZE = 4; // or however many events per page
    private DocumentSnapshot lastVisible = null;
    private final Timestamp now = Timestamp.now();

    @SuppressLint("NotifyDataSetChanged")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initializing view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // initializing recycler view
        recyclerView = binding.homeEventRecyclerView;
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // initializing events list
        if (userViewModel.getHomeEvents().getValue() != null) {
            events = userViewModel.getHomeEvents().getValue();
        } else {
            events = new ArrayList<>();
        }

        // initializing and attaching adapter
        adapter = new EventCardAdapter(events, position -> {
            // update the view model
            userViewModel.setSelectedEvent(events.get(position));

            // open the event fragment
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_home_to_eventDetail);
        });

        recyclerView.setAdapter(adapter);

        // detects when user reaches end
        isLoading = false;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // only check when scrolling down
                if (dy <= 0) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                        && firstVisibleItemPosition >= 0) {
                    loadNextPage();
                }
            }
        });

        // if data is not cached, load first page
        // also ensures that data is only loaded if user is accessible
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && events.isEmpty()) {
                loadFirstPage();
            }
        });

        // initialize button for opening filter dialogue
        binding.filterButton.setOnClickListener(v -> openFilterDialogue());



        binding.refreshButton.setOnClickListener(v -> {
            applyFiltersAndReload();
        });


        // initialize button for qr code scanning
        binding.openQrFragmentButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openScannerDialog();
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        // update the view model
        userViewModel.setHomeEvents(events);
    }

    /**
     * Launches the camera permission dialog
     */
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openScannerDialog();
                } else {
                    Toast.makeText(requireContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                }
            });

    /**
     * Opens the QR scanner dialog
     */
    private void openScannerDialog() {
        QRScannerDialogFragment dialog = new QRScannerDialogFragment();
        dialog.setListener(qr -> {
            firebaseManager.getEvent(qr, new FirebaseCallback<Event>() {

                @Override
                public void onSuccess(Event result) {
                    userViewModel.setSelectedEvent(result);
                    NavController navController = NavHostFragment.findNavController(HomeFragment.this);
                    navController.navigate(R.id.action_home_to_eventDetail);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show(getChildFragmentManager(), "QRScannerDialog");

    }

    /**
     * Opens the filter dialogue
     *
     * <p>Sets filters in the dialogue, and apply listener</p>
     */
    /**
     * Opens the filter dialogue and applies selected category filters to events.
     */
    private void openFilterDialogue() {
        FilterDialogueFragment dialog = new FilterDialogueFragment();

        // pre-select filters in the dialog
        dialog.setPreselectedCategories(selectedFilters);

        // listen for filter selections
        dialog.setOnFilterSelectedListener(filters -> {
            selectedFilters = new ArrayList<>(filters);

            // apply filters and reload events
            applyFiltersAndReload();
        });

        dialog.show(getParentFragmentManager(), "FilterDialog");
    }


    /**
     * Builds the Firestore query for events with pagination and selected filters.
     *
     * @param startAfter The last DocumentSnapshot from previous page for pagination, or null for first page.
     * @return Firestore Query ready to execute.
     */
    private Query buildEventQuery(DocumentSnapshot startAfter) {
        String userId = userViewModel.getUser().getValue().getUid();

        Query query = firebaseManager.getEventsReference()
                .orderBy("Event Date", Query.Direction.DESCENDING)
                .whereGreaterThan("End Date", now);

        // Exclude user's own events
        if (userId != null && !userId.isEmpty()) {
            query = query.whereNotEqualTo("Organizer", userId);
        }

        // Apply category filters only if they exist and <= 10
        if (!selectedFilters.isEmpty() && selectedFilters.size() <= 10) {
            query = query.whereArrayContainsAny("Categories", selectedFilters);
        }

        // Pagination
        if (startAfter != null) {
            query = query.startAfter(startAfter);
        }

        // Limit results per page
        query = query.limit(PAGE_SIZE);

        return query;
    }


    private void loadFirstPage() {
        isLoading = true;
        String userId = userViewModel.getUser().getValue().getUid();

        Query query = firebaseManager.getEventsReference()
                .orderBy("Event Date", Query.Direction.DESCENDING)
                .whereGreaterThan("End Date", now)
                .whereNotEqualTo("Organizer", userId)
                .limit(PAGE_SIZE);

        firebaseManager.getQuery(null, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                int startPos = events.size();

                for (DocumentSnapshot doc : result) {
                    Event event = firebaseManager.documentToEvent(doc);
                    if (selectedFilters.isEmpty() || !Collections.disjoint(event.getCategories(), selectedFilters)) {
                        events.add(event);
                    }
                }

                adapter.notifyItemRangeInserted(startPos, events.size() - startPos);
                isLoading = false;
                lastVisible = result.isEmpty() ? null : result.get(result.size() - 1);
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("Firebase", "Failed to load first page", e);
                isLoading = false;
            }
        });
    }

    private void loadNextPage() {
        if (isLoading || lastVisible == null) return;
        isLoading = true;
        String userId = userViewModel.getUser().getValue().getUid();

        Query query = firebaseManager.getEventsReference()
                .orderBy("Event Date", Query.Direction.DESCENDING)
                .whereGreaterThan("End Date", now)
                .whereNotEqualTo("Organizer", userId)
                .startAfter(lastVisible)
                .limit(PAGE_SIZE);

        firebaseManager.getQuery(lastVisible, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                int startPos = events.size();

                for (DocumentSnapshot doc : result) {
                    Event event = firebaseManager.documentToEvent(doc);
                    if (selectedFilters.isEmpty() || !Collections.disjoint(event.getCategories(), selectedFilters)) {
                        events.add(event);
                    }
                }

                adapter.notifyItemRangeInserted(startPos, events.size() - startPos);
                isLoading = false;
                lastVisible = result.isEmpty() ? null : result.get(result.size() - 1);
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("Firebase", "Failed to load next page", e);
                isLoading = false;
            }
        });
    }


    /**
     * Builds and executes a query to Firestore with pagination and filters.
     *
     * @param startAfter DocumentSnapshot to start after (for pagination), null for first page
     */
    private void executeEventQuery(DocumentSnapshot startAfter) {
        String userId = userViewModel.getUser().getValue().getUid();

        Query query = firebaseManager.getEventsReference()
                .orderBy("Event Date", Query.Direction.DESCENDING)
                .whereGreaterThan("End Date", now);

        if (userId != null && !userId.isEmpty()) {
            query = query.whereNotEqualTo("Organizer", userId);
        }

        if (!selectedFilters.isEmpty() && selectedFilters.size() <= 10) {
            query = query.whereArrayContainsAny("Categories", selectedFilters);
        }

        if (startAfter != null) {
            query = query.startAfter(startAfter);
        }

        query = query.limit(PAGE_SIZE);

        firebaseManager.getQuery(startAfter, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                Log.d("Firebase", "Fetched " + result.size() + " documents with filters: " + selectedFilters);

                int startPos = events.size();
                for (DocumentSnapshot doc : result) {
                    events.add(firebaseManager.documentToEvent(doc));
                }

                adapter.notifyItemRangeInserted(startPos, result.size());
                isLoading = false;

                if (!result.isEmpty()) {
                    lastVisible = result.get(result.size() - 1);
                } else {
                    lastVisible = null; // no more pages
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firebase", "Failed to fetch events", e);
                isLoading = false;
            }
        });
    }

    /**
     * Applies currently selected category filters and reloads events from Firestore.
     * Clears previous events and resets pagination.
     */
    private void applyFiltersAndReload() {
        events.clear();
        adapter.notifyDataSetChanged();
        lastVisible = null;
        loadFirstPage();
    }




}