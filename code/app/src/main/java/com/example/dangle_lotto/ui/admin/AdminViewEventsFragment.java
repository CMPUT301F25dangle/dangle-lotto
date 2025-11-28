package com.example.dangle_lotto.ui.admin;

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
import androidx.navigation.NavHostController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentAdminViewEventsBinding;
import com.example.dangle_lotto.ui.EventCardAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AdminViewEventsFragment - This fragment is displayed when an admin user wants
 * to view all the events. Admins can view events and delete any events that
 * go against app policy.
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-22
 */
public class AdminViewEventsFragment extends Fragment {
    private FragmentAdminViewEventsBinding binding;
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private ArrayList<Event> events;
    private RecyclerView recyclerView;
    private EventCardAdapter adapter;
    private AdminViewModel adminViewModel;
    private LinearLayoutManager manager;
    private static final int PAGE_SIZE = 4;
    private boolean isLoading;
    private DocumentSnapshot lastVisible = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminViewEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        adminViewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        recyclerView = binding.adminEventsList;
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        events = new ArrayList<>();

        adapter = new EventCardAdapter(events, position -> {
            firebaseManager.getEvent(events.get(position).getEid(), new FirebaseCallback<Event>() {
                @Override
                public void onSuccess(Event result) {
                    adminViewModel.setSelectedEvent(result);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firebase", "failed to fetch selected event", e);
                }
            });
        });
        recyclerView.setAdapter(adapter);

        adminViewModel.getSelectedEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.action_adminViewEventsFragment_to_admin_eventdetails_fragment);
            }
        });

        isLoading = false;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // only check when scrolling down
                if (dy <= 0) return;

                int visibleItemCount = manager.getChildCount();
                int totalItemCount = manager.getItemCount();
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                        && firstVisibleItemPosition >= 0) {
                    loadNextPage();
                }
            }
        });
        getParentFragmentManager().setFragmentResultListener("eventDeleted", this, (requestKey, result) -> {
            String deleted= result.getString("deletedPosition");
            for (Event event: events){
                if (Objects.equals(event.getEid(), deleted)){
                    events.remove(event);
                    break;
                }
            }
        });
        loadFirstPage();
        return root;

    }

    /**
     * loads first page of events querying firebase
     */
    private void loadFirstPage() {
        isLoading = true;
        Query query = firebaseManager.getEventsReference().orderBy("Event Date", Query.Direction.DESCENDING).limit(PAGE_SIZE);
        firebaseManager.getQuery(null, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                int startPos = events.size();
                for (DocumentSnapshot doc : result) {
                    events.add(firebaseManager.documentToEvent(doc));
                }
                adapter.notifyDataSetChanged();
                isLoading = false;
                if (!result.isEmpty()) {
                    lastVisible = result.get(result.size() - 1);
                } else {
                    Log.e("Firebase", "Noe more pages");
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
     * loads next page by querying
     */
    private void loadNextPage() {
        if (isLoading || lastVisible == null) {
            return;
        }
        isLoading = true;
        Toast.makeText(getContext(), "Loading more events...", Toast.LENGTH_SHORT).show();
        Query query = firebaseManager.getEventsReference().orderBy("Event Date", Query.Direction.DESCENDING).limit(PAGE_SIZE);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }
}
