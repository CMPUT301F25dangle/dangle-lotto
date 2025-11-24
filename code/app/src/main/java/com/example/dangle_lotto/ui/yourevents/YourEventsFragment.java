package com.example.dangle_lotto.ui.yourevents;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentHomeBinding;
import com.example.dangle_lotto.databinding.FragmentYourEventsBinding;
import com.example.dangle_lotto.ui.EventCardAdapter;
import com.example.dangle_lotto.ui.dashboard.OrganizerEventDetailsEventFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class YourEventsFragment extends Fragment {
    private FragmentYourEventsBinding binding;
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
    private int chunkIndex = 0;
    private ArrayList<String> interactedIds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentYourEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initializing view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // initializing recycler view
        recyclerView = binding.yourEventsRecyclerView;
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // initializing events list
        if (userViewModel.getYourEvents().getValue() != null) {
            events = userViewModel.getYourEvents().getValue();
        } else {
            events = new ArrayList<>();
        }

        // initializing interacted ids
        interactedIds = new ArrayList<>();

        // initializing and attaching adapter
        adapter = new EventCardAdapter(events, position -> {
            // update the view model
            userViewModel.setSelectedYourEvent(events.get(position));

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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        // update the view model
        userViewModel.setYourEvents(events);
    }

    public void getUserAllInteractions(String uid, FirebaseCallback<ArrayList<String>> callback) {
        ArrayList<String> all = new ArrayList<>();
        String[] cols = {"Chosen", "Register", "Signup", "Cancelled"};

        final int[] remaining = {cols.length}; // use so that we can count how many subcollections we have looked through

        for (String col : cols) {
            firebaseManager.getUserSubcollection(uid, col, new FirebaseCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> ids) {
                    all.addAll(ids);
                    remaining[0]--;

                    if (remaining[0] == 0) {
                        callback.onSuccess(all);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    remaining[0]--;

                    if (remaining[0] == 0) {
                        callback.onSuccess(all); // return what we have
                    }
                }
            });
        }
    }


    /**
     * Loads the first page of events by querying firebase
     */
    private void loadFirstPage() {
        isLoading = true;
        String userId = userViewModel.getUser().getValue().getUid();

        getUserAllInteractions(userId, new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> ids) {
                interactedIds = ids;

                if (ids.isEmpty()) {
                    isLoading = false;
                    return;
                }

                // take first chunk of 10
                List<String> chunk = interactedIds.subList(0, Math.min(10, interactedIds.size()));

                Query query = firebaseManager.getEventsReference()
                        .whereIn(FieldPath.documentId(), chunk)
                        .orderBy("Date", Query.Direction.DESCENDING)
                        .limit(PAGE_SIZE);

                firebaseManager.getQuery(
                        null,
                        query,
                        new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
                            @Override
                            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                                int startPos = events.size();
                                for (DocumentSnapshot doc : result) {
                                    events.add(firebaseManager.documentToEvent(doc));
                                }

                                chunkIndex = 1;
                                adapter.notifyItemRangeInserted(startPos, result.size());

                                isLoading = false;
                                lastVisible = result.isEmpty()
                                        ? null
                                        : result.get(result.size() - 1);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                isLoading = false;
                            }
                        }
                );
            }

            @Override
            public void onFailure(Exception e) {
                // If the user interactions could not load,
                // we still need to stop loading state.
                isLoading = false;
            }
        });
    }

    /**
     * Loads the next page of events by querying firebase
     */
    private void loadNextPage() {
        if (isLoading) return;
        isLoading = true;

        int start = chunkIndex * 10;
        if (start >= interactedIds.size()) {
            isLoading = false;
            return; // no more chunks
        }

        int end = Math.min(start + 10, interactedIds.size());
        List<String> chunk = interactedIds.subList(start, end);
        chunkIndex++;

        Query query = firebaseManager.getEventsReference()
                .whereIn(FieldPath.documentId(), chunk)
                .orderBy("Date", Query.Direction.DESCENDING)
                .limit(chunk.size()); // match size of chunk

        firebaseManager.getQuery(null, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                int startPos = events.size();
                for (DocumentSnapshot doc : result) {
                    events.add(firebaseManager.documentToEvent(doc));
                }

                adapter.notifyItemRangeInserted(startPos, result.size());
                isLoading = false;
            }

            @Override
            public void onFailure(Exception e) {
                isLoading = false;
            }
        });
    }
}