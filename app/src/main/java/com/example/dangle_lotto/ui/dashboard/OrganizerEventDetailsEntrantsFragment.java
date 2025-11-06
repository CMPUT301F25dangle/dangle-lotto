package com.example.dangle_lotto.ui.dashboard;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.FirestoreCallback;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.databinding.FragmentOrganizerEventDetailsEntrantsBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


/**
 *
 */
public class OrganizerEventDetailsEntrantsFragment extends Fragment {
    private FragmentOrganizerEventDetailsEntrantsBinding binding;
    private FirebaseManager firebase;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> entrantNames = new ArrayList<>();
    private ListView listView;
    private View progress;
    private View emptyView;
    private String eid() {
        return getArguments() != null ? getArguments().getString("eid") : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerEventDetailsEntrantsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebase = new FirebaseManager();

        listView = binding.entrantsListView;
        progress = binding.progress;
        emptyView = binding.emptyView;

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, entrantNames);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

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

        String eventId = eid();
        if (eventId == null) {
            if (progress != null) progress.setVisibility(View.GONE);
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (listView != null) listView.setVisibility(View.INVISIBLE);
            return;
        }

        // Pull UIDs from events/{eid}/Register (entrants live here)
        firebase.getEventSubcollection(eventId, "Register", new FirestoreCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> uids) {
                if (uids == null || uids.isEmpty()) {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                    if (listView != null) listView.setVisibility(View.INVISIBLE);
                    return;
                }

                final int total = uids.size();
                final int[] done = {0};

                for (String uid : uids) {
                    firebase.getUser(uid, new FirestoreCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            entrantNames.add(user.getName() + " (" + user.getEmail() + ")");
                            finishOne();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Fallback to UID so the entrant is still visible
                            entrantNames.add(uid);
                            finishOne();
                        }

                        private void finishOne() {
                            done[0]++;
                            if (done[0] == total) {
                                if (adapter != null) adapter.notifyDataSetChanged();
                                if (progress != null) progress.setVisibility(View.GONE);
                                boolean isEmpty = entrantNames.isEmpty();
                                if (emptyView != null) emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                                if (listView != null) listView.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (progress != null) progress.setVisibility(View.GONE);
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                if (listView != null) listView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}