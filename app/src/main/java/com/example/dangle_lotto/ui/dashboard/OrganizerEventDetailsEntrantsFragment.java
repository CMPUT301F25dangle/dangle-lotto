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
    private String eid() {
        return getArguments() != null ? getArguments().getString("eid") : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerEventDetailsEntrantsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebase = new FirebaseManager();
        ListView listView = binding.entrantsListView;

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,entrantNames);
        listView.setAdapter(adapter);

        loadEntrants();
        return root;

    }

    private void loadEntrants() {
        String eventId = eid();
        if (eventId == null) return;

        firebase.getEventSignUps(eventId, task -> {
            if (!task.isSuccessful()) return;
            QuerySnapshot qs = task.getResult();
            if (qs == null || qs.isEmpty()) return;

            final int total = qs.size();
            final int[] count = {0};

            for (DocumentSnapshot doc : qs.getDocuments()) {
                String uid = doc.getId();
                firebase.getUser(uid, new FirestoreCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        entrantNames.add(user.getName() + " (" + user.getEmail() + ")");
                        count[0]++;
                        checkDone(count[0], total);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        count[0]++;
                        checkDone(count[0], total);
                    }

                    private void checkDone(int done, int total) {
                        if (done == total) adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}