package com.example.dangle_lotto.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;

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
    private LinearLayoutManager manager;
    private static final int PAGE_SIZE = 4;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminViewEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.adminEventsList;
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        events = new ArrayList<>();
        firebaseManager.getAllEvents(new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                if (!events.isEmpty()){
                    events.clear();
                }

                for (String id : result) {
                    firebaseManager.getEvent(id, new FirebaseCallback<Event>() {
                        @Override
                        public void onSuccess(Event result) {
                            events.add(result);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("Firebase", "failed to fetch event" + id, e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firebase", "failed to fetch events", e);
            }
        });

        adapter = new EventCardAdapter(events, position -> {
            NavController navController = NavHostFragment.findNavController(this);
//            navController.navigate(R.id.act_);
        });
        recyclerView.setAdapter(adapter);
        return root;

    }
}
