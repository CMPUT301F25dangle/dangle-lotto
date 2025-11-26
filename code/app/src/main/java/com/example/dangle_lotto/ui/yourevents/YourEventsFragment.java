package com.example.dangle_lotto.ui.yourevents;

import android.annotation.SuppressLint;
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
import android.widget.Button;
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
import java.util.Collection;
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
    private ArrayList<String> interactedIds;
    private Button[] buttons;
    Query query;

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

        events = new ArrayList<>();

        // initializing interacted ids
        interactedIds = new ArrayList<>();

        // initializing and attaching adapter
        adapter = new EventCardAdapter(events, position -> {
            // update the view model
            userViewModel.setSelectedEvent(events.get(position));
            // open the event fragment
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_navigation_your_events_to_event_detail_fragment);
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

        Button registerButton = binding.yourEventsRegistered;
        Button chosenButton = binding.yourEventsChosen;
        Button signUpButton = binding.yourEventsSignups;
        Button cancelledButton = binding.yourEventsCancelled;

        buttons = new Button[]{registerButton, chosenButton, signUpButton, cancelledButton};

        for (Button button : buttons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTabButtonClicked((Button) v);
                }
            });
        }

        // initialization of the fragment
        onTabButtonClicked(registerButton);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        // update the view model
        userViewModel.setYourEvents(events);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onTabButtonClicked(Button clickedButton) {
        for (Button b : buttons) {
            b.setSelected(false);
        }
        clickedButton.setSelected(true);

        String uid = userViewModel.getUser().getValue().getUid();

        events.clear();
        lastVisible = null;
        adapter.notifyDataSetChanged();

        if (clickedButton == binding.yourEventsRegistered) {
            query = firebaseManager.getUsersReference()
                    .document(uid)
                    .collection("Register")
                    .orderBy("Timestamp", Query.Direction.DESCENDING);
        } else if (clickedButton == binding.yourEventsChosen) {
            query = firebaseManager.getUsersReference()
                    .document(uid)
                    .collection("Chosen")
                    .orderBy("Timestamp", Query.Direction.DESCENDING);
        } else if (clickedButton == binding.yourEventsSignups){
            query = firebaseManager.getUsersReference()
                    .document(uid)
                    .collection("SignUps")
                    .orderBy("Timestamp", Query.Direction.DESCENDING);
        }else{
            query = firebaseManager.getUsersReference()
                    .document(uid)
                    .collection("Cancelled")
                    .orderBy("Timestamp", Query.Direction.DESCENDING);
        }

        loadFirstPage();
    }

    /**
     * Gets the event object and adds it to the list of events. Notifies adapter of data change
     *
     * @param eid Unique event identifier.
     *
     */
    private void loadEventDetails(String eid){
        firebaseManager.getEvent(eid, new FirebaseCallback<Event>() {

            @Override
            public void onSuccess(Event result) {
                events.add(result);
                adapter.notifyItemInserted(events.size() - 1);
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("Firebase", "Failed to load event details", e);
            }
        });
    }

    /**
     * Loads the first page of events by querying firebase
     */
    private void loadFirstPage() {
        isLoading = true;
        firebaseManager.getQuery(null, query.limit(PAGE_SIZE), new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                for (DocumentSnapshot doc : result) {
                    loadEventDetails(doc.getId());
                }

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
        firebaseManager.getQuery(lastVisible, query.startAfter(lastVisible).limit(PAGE_SIZE), new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                Log.d("Firebase", "Loaded " + result.size() + " events");
                for (DocumentSnapshot doc : result) {
                    loadEventDetails(doc.getId());
                }

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