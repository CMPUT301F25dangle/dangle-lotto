package com.example.dangle_lotto.ui.dashboard;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * OrganizerEventDetailsEntrantsFragment - Fragment shows entrants of an event.
 * <p>
 * Here user can filter the users that have applied to an event in any way.
 *
 * @author Fogil Zheng
 * @version 1.0
 * @since 2025-11-06
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
    private ChipGroup filterGroup;
    private Chip chipRegistrants, chipChosen, chipSignups, chipCancelled;
    private Button dynamicButton;
    private Button removeButton;

    private enum Filter { REGISTRANTS, CHOSEN, SIGNUPS, CANCELLED }
    private Filter currentFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // binding
        binding = FragmentOrganizerEventDetailsEntrantsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // view model + event
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        event = userViewModel.getSelectedOrganizedEvent().getValue();

        // firebase
        firebase = new FirebaseManager();

        // button
        dynamicButton = binding.eventDetailsEntrantsButton;
        removeButton = binding.eventDetailsEntrantsRemoveButton;

        // views
        listView  = binding.entrantsListView;
        progress  = binding.progress;
        emptyView = binding.emptyView;

        // adapter
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, entrantNames);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);

        // --- FILTER CHIPS ---
        filterGroup     = binding.filterGroup;      // <ChipGroup android:id="@+id/filterGroup"/>
        chipRegistrants = binding.chipRegistrants;  // <Chip android:id="@+id/chip_registrants"/>
        chipChosen      = binding.chipChosen;       // <Chip android:id="@+id/chip_chosen"/>
        chipSignups     = binding.chipSignups;      // <Chip android:id="@+id/chip_signups"/>
        chipCancelled   = binding.chipCancelled;    // <Chip android:id="@+id/chip_cancelled"/>

        // default filter
        currentFilter = Filter.REGISTRANTS;
        chipRegistrants.setChecked(true);

        // when a chip is selected, reload with that filter
        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);

            if (id == chipRegistrants.getId()) {
                currentFilter = Filter.REGISTRANTS;
                dynamicButton.setText("Choose:");
                removeButton.setVisibility(View.GONE);
            } else if (id == chipChosen.getId()) {
                currentFilter = Filter.CHOSEN;
                dynamicButton.setText("Notify");
                removeButton.setVisibility(View.VISIBLE);
            } else {
                if (id == chipSignups.getId()) currentFilter = Filter.SIGNUPS;
                else if (id == chipCancelled.getId()) currentFilter = Filter.CANCELLED;

                dynamicButton.setText("Notify");
                removeButton.setVisibility(View.GONE);
            }

            loadEntrants(currentFilter);
        });


        dynamicButton.setOnClickListener(v -> {
            if (currentFilter == Filter.REGISTRANTS) {
                event.chooseLottoWinners();
                loadEntrants(currentFilter);
            } else {
                // can add manual notify functionality here
            }
        });

        removeButton.setOnClickListener(v -> {
            if (currentFilter == Filter.CHOSEN) {
                event.cancelAllChosen();
                loadEntrants(currentFilter);
            }
        });

        // initial load
        loadEntrants(currentFilter);
        return root;
    }

    /**
     * Loads entrants based on the filter
     *
     * @param filter Filter to use
     */
    private void loadEntrants(Filter filter) {
        // UI: start loading
        if (progress != null) progress.setVisibility(View.VISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (listView != null) listView.setVisibility(View.INVISIBLE);

        entrantNames.clear();
        if (adapter != null) adapter.notifyDataSetChanged();

        // Use the event from your ViewModel
        String eventId = (event != null) ? event.getEid() : null;
        if (eventId == null) {
            if (progress != null) progress.setVisibility(View.GONE);
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (listView != null) listView.setVisibility(View.INVISIBLE);
            return;
        }

        // Sets we’ll fill from subcollections
        final Set<String> register  = new HashSet<>();
        final Set<String> chosen    = new HashSet<>();
        final Set<String> cancelled = new HashSet<>();
        final Set<String> signsup   = new HashSet<>();

        // Count EXACTLY how many fetches we start
        final int[] need = {0};
        final int[] done = {0};

        Runnable onEachFetchDone = () -> {
            done[0]++;
            if (done[0] == need[0]) {
                // Decide which UIDs to show based on filter
                Set<String> toShow;
                switch (filter) {
                    case REGISTRANTS:
                        toShow = register;
                        break;
                    case CHOSEN:
                        toShow = chosen;
                        break;
                    case CANCELLED:
                        toShow = cancelled;
                        break;
                    case SIGNUPS:
                        toShow = signsup;
                        break;
                    default:
                        toShow = register;
                }
                resolveUsersToNames(new ArrayList<>(toShow));
            }
        };

        // Start ONLY the fetches needed for the selected filter
        switch (filter) {
            case REGISTRANTS:
                need[0]++;
                fetchSubcollectionIntoSet(eventId, "Register", register, onEachFetchDone);
                break;

            case CHOSEN:
                need[0]++;
                fetchSubcollectionIntoSet(eventId, "Chosen", chosen, onEachFetchDone);
                break;

            case CANCELLED:
                need[0]++;
                fetchSubcollectionIntoSet(eventId, "Cancelled", cancelled, onEachFetchDone);
                break;

            case SIGNUPS:
                need[0]++;
                fetchSubcollectionIntoSet(eventId, "SignUps",  signsup, onEachFetchDone);
                break;
        }
    }

    /**
     * Fetches a subcollection into a set
     *
     * @param eid Event ID
     * @param sub Subcollection
     * @param out Set to add to
     * @param onDone Runnable to run when done
     */
    private void fetchSubcollectionIntoSet(String eid, String sub, Set<String> out, Runnable onDone) {
        firebase.getEventSubcollection(eid, sub, new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> ids) {
                if (ids != null) out.addAll(ids);
                onDone.run();
            }
            @Override
            public void onFailure(Exception e) {
                // treat as empty
                onDone.run();
            }
        });
    }

    /**
     * Resolves a list of UIDs to names
     *
     * @param uids List of UIDs
     */
    private void resolveUsersToNames(List<String> uids) {
        if (uids == null || uids.isEmpty()) {
            finishUIUpdate();
            return;
        }

        final int total = uids.size();
        final int[] done = {0};

        for (String uid : uids) {
            firebase.getUser(uid, new FirebaseCallback<GeneralUser>() {
                @Override
                public void onSuccess(GeneralUser user) {
                    entrantNames.add(user.getName() + " (" + user.getEmail() + ")");
                    finishOne();
                }
                @Override
                public void onFailure(Exception e) {
                    entrantNames.add(uid);
                    finishOne();
                }
                private void finishOne() {
                    done[0]++;
                    if (done[0] == total) finishUIUpdate();
                }
            });
        }
    }

    /**
     * Finishes the UI update
     */
    private void finishUIUpdate() {
        if (adapter != null) adapter.notifyDataSetChanged();
        if (progress != null) progress.setVisibility(View.GONE);
        boolean isEmpty = entrantNames.isEmpty();
        if (emptyView != null) emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (listView != null) listView.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}