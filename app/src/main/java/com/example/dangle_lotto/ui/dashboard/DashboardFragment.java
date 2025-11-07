package com.example.dangle_lotto.ui.dashboard;

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

import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.databinding.FragmentDashboardBinding;
import com.example.dangle_lotto.ui.EventCardAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private UserViewModel userViewModel;
    private FirebaseManager firebaseManager = new FirebaseManager();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private EventCardAdapter adapter;
    private ArrayList<Event> organizedEvents;
    private User user;
    private boolean isLoading;
    private static final int PAGE_SIZE = 4; // or however many events per page
    private DocumentSnapshot lastVisible = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // initializing view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // initializing binding
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // set text and image views with user info
        user = userViewModel.getUser().getValue();
        binding.dashboardFragmentUsername.setText(user.getName());
        // binding.dashboardFragmentUserPicture.setImageResource(user.getPhotoId());

        // setting button on click
        binding.dashboardFragmentSettingButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_navigation_dashboard_to_userSettingFragment);
        });

        // initializing recycler view
        recyclerView = binding.dashboardFragmentRecyclerView;
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // initializing events list
        if (userViewModel.getOrganizedEvents().getValue() != null) {
            organizedEvents = userViewModel.getOrganizedEvents().getValue();
        } else {
            organizedEvents = new ArrayList<>();
        }

        // initializing and attaching adapter
        adapter = new EventCardAdapter(organizedEvents, position -> {
            // update the view model
            userViewModel.setSelectedOrganizedEvent(organizedEvents.get(position));

            // open the event fragment
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_navigation_dashboard_to_organizerEventDetailsFragment);
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
        if (organizedEvents.isEmpty()) {
            loadFirstPage();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        // update the view model
        userViewModel.setOrganizedEvents(organizedEvents);
    }

    /**
     * Loads the first page of events by querying firebase
     */
    private void loadFirstPage() {
        isLoading = true;

        firebaseManager.getOrganizedEventsQuery(null, user.getUid(), PAGE_SIZE, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                int startPos = organizedEvents.size();
                for (DocumentSnapshot doc : result) {
                    organizedEvents.add(firebaseManager.documentToEvent(doc));
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
        if (isLoading || lastVisible == null) return;
        isLoading = true;
        Toast.makeText(getContext(), "Loading more events...", Toast.LENGTH_SHORT).show();
        firebaseManager.getOrganizedEventsQuery(lastVisible, user.getUid(), PAGE_SIZE, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                Log.d("Firebase", "Loaded " + result.size() + " events");
                int startPos = organizedEvents.size();
                for (DocumentSnapshot doc : result) {
                    organizedEvents.add(firebaseManager.documentToEvent(doc));
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
                Log.d("Firebase", "Failed to load next page", e);
                isLoading = false;

            }
        });
    }
}