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
import com.example.dangle_lotto.FirebaseIdlingResource;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentHomeBinding;
import com.example.dangle_lotto.ui.EventCardAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

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
    private FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private UserViewModel userViewModel;
    private ArrayList<Event> events;
    private EventCardAdapter adapter;
    private LinearLayoutManager layoutManager;
    private boolean isLoading;
    private static final int PAGE_SIZE = 4; // or however many events per page
    private DocumentSnapshot lastVisible = null;

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
            userViewModel.setSelectedHomeEvent(events.get(position));

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
                loadFirstPage();  // SAFE now
            }
        });

        // initialize button for opening filter dialogue
        binding.filterButton.setOnClickListener(v -> openFilterDialogue());

        binding.refreshButton.setOnClickListener(v -> {
           userViewModel.setHomeEvents(null);
           events.clear();
           adapter.notifyDataSetChanged();
           loadFirstPage();
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

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openScannerDialog();
                } else {
                    Toast.makeText(requireContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                }
            });

    private void openScannerDialog() {
        QRScannerDialogFragment dialog = new QRScannerDialogFragment();
        dialog.setListener(qr -> {
            firebaseManager.getEvent(qr, new FirebaseCallback<Event>() {

                @Override
                public void onSuccess(Event result) {
                    userViewModel.setSelectedHomeEvent(result);
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

    /**
     * Loads the first page of events by querying firebase
     */
    private void loadFirstPage() {
        isLoading = true;
        String userId = userViewModel.getUser().getValue().getUid();

        Query query = firebaseManager.getEventsReference()
                .orderBy("Date", Query.Direction.DESCENDING)
                .whereNotEqualTo("Organizer", userId)
                .limit(PAGE_SIZE);
        firebaseManager.getQuery(null, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                int startPos = events.size();
                for (DocumentSnapshot doc : result) {
                    events.add(firebaseManager.documentToEvent(doc));
                }

                adapter.notifyItemRangeInserted(startPos, result.size());
                isLoading = false;
                if (!result.isEmpty()) {
                    lastVisible = result.get(result.size() - 1);

                } else {
                    // No more pages
                    lastVisible = null;
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("Firebase", "Failed to load first page", e);
                isLoading = false;

            }
        });
    }

    /**
     * Loads the next page of events by querying firebase
     */
    private void loadNextPage() {
        String userId = userViewModel.getUser().getValue().getUid();

        if (isLoading || lastVisible == null) return;
        isLoading = true;
        Toast.makeText(getContext(), "Loading more events...", Toast.LENGTH_SHORT).show();
        Query query = firebaseManager.getEventsReference()
                .orderBy("Date", Query.Direction.DESCENDING)
                .whereNotEqualTo("Organizer", userId)
                .startAfter(lastVisible)
                .limit(PAGE_SIZE);
        firebaseManager.getQuery(lastVisible, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                Log.d("Firebase", "Loaded " + result.size() + " events");
                int startPos = events.size();
                for (DocumentSnapshot doc : result) {
                    events.add(firebaseManager.documentToEvent(doc));
                }

                adapter.notifyItemRangeInserted(startPos, result.size());
                isLoading = false;
                if (!result.isEmpty()) {
                    lastVisible = result.get(result.size() - 1);
                } else {
                    // THIS MAY NEED FIXING, DOES NOT WORK IF NEW EVENTS ARE ADDED DURING RUNTIME SHOULD PROLLY IMPLEMENT A REFRESH
                    // ADD REFRESH THROUGH BUTTON
                    // No more pages
                    lastVisible = null;
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("Firebase", "Failed to load next page", e);
                isLoading = false;

            }
        });
    }


}