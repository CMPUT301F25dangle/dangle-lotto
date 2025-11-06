package com.example.dangle_lotto.ui.dashboard;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.GeneralUser;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.databinding.FragmentOrganizerEventDetailsEntrantsBinding;

import java.util.ArrayList;


/**
 *
 */
public class OrganizerEventDetailsEntrantsFragment extends Fragment {
    private FragmentOrganizerEventDetailsEntrantsBinding binding;
    private UserViewModel userViewModel;
    private FirebaseManager firebase;
    private Event event;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> entrantNames = new ArrayList<>();
    private ListView listView;
    private View progress;
    private View emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // initializing binding
        binding = FragmentOrganizerEventDetailsEntrantsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initializing view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // getting event from view model
        event = userViewModel.getSelectedOrganizedEvent().getValue();

        // initializing firebase
        firebase = new FirebaseManager();

        // initializing views
        listView = binding.entrantsListView;
        progress = binding.progress;
        emptyView = binding.emptyView;

        // initializing adapter and setting to list view
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, entrantNames);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        // loading entrants and adding to array list
        loadEntrants();
        return root;
    }

    private void loadEntrants() {
        // UI: start loading
        if (progress != null) progress.setVisibility(View.VISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (listView != null) listView.setVisibility(View.INVISIBLE);

        entrantNames.clear();
        if (adapter != null) adapter.notifyDataSetChanged();

        String eventId = event.getEid();
        if (eventId == null) {
            if (progress != null) progress.setVisibility(View.GONE);
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (listView != null) listView.setVisibility(View.INVISIBLE);
            return;
        }

        // Pull UIDs from events/{eid}/Register (entrants live here)
        firebase.getEventSubcollection(eventId, "Register", new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> uids) {
                if (uids == null || uids.isEmpty()) {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                    if (listView != null) listView.setVisibility(View.INVISIBLE);
                    return;
                }

                for (String uid : uids) {
                    firebase.getUser(uid, new FirebaseCallback<GeneralUser>() {
                        @Override
                        public void onSuccess(GeneralUser user) {
                            entrantNames.add(user.getName() + " (" + user.getEmail() + ")");
                            finishOne();
                            Log.d("OrganizerEventDetailsEntrantsFragment", "Entrant: " + user.getName());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Fallback to UID so the entrant is still visible
                            entrantNames.add(uid);
                            finishOne();
                        }

                        private void finishOne() {
                            if (adapter != null) adapter.notifyDataSetChanged();
                            if (progress != null) progress.setVisibility(View.GONE);
                            boolean isEmpty = entrantNames.isEmpty();
                            if (emptyView != null) emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                            if (listView != null) listView.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (progress != null) progress.setVisibility(View.GONE);
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                if (listView != null) listView.setVisibility(View.INVISIBLE);
                Log.d("OrganizerEventDetailsEntrantsFragment", "Error getting entrants: " + e);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}