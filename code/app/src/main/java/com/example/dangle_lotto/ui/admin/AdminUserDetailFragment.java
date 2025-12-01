package com.example.dangle_lotto.ui.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.GeneralUser;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.databinding.FragmentAdminUserDetailBinding;
import com.example.dangle_lotto.ui.EventCardAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

/**
 * AdminUserDetailFragment - this fragment is displayed when an admin selects a user
 * Admins can view the details and delete the user.
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-29
 */
public class AdminUserDetailFragment extends Fragment {
    private FragmentAdminUserDetailBinding binding;
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private AdminViewModel adminViewModel;
    private User selectedUser;
    private EventCardAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private ArrayList<Event> events;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminUserDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize ViewModel
        adminViewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        selectedUser = adminViewModel.getSelectedUser().getValue();
        recyclerView = binding.adminDashboardList;
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        if (events == null) {
            events = new ArrayList<>();
        } else {
            events.clear();
        }

        adapter = new EventCardAdapter(events, position -> {

        });
        recyclerView.setAdapter(adapter);
        // get all events user organized
        Query query = firebaseManager.getEventsReference().whereEqualTo("Organizer", selectedUser.getUid()).orderBy("Event Date", Query.Direction.DESCENDING);
        firebaseManager.getQuery(null, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                int startPos = events.size();
                for (DocumentSnapshot doc : result) {
                    events.add(firebaseManager.documentToEvent(doc));
                }

                adapter.notifyItemRangeInserted(startPos, result.size());

            }

            @Override
            public void onFailure(Exception e) {
                Log.d("Firebase", "Failed to load events", e);
            }
        });

        // set user details
        binding.adminDashboardFragmentUsername.setText(selectedUser.getUsername());
        if (!(selectedUser.getPhotoID().isEmpty() || selectedUser.getPhotoID() == null)) {
            Glide.with(requireContext()).load(selectedUser.getPhotoID()).into(binding.adminDashboardFragmentUserPicture);
        }

        // Set back button
        binding.adminUserBtnBack.setOnClickListener(v -> {
            adminViewModel.setSelectedUser(null);
            Navigation.findNavController(v).popBackStack();
        });

        firebaseManager.getUsersReference().document(selectedUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        binding.adminSwitchOrganizer.setChecked(documentSnapshot.getBoolean("CanOrganize"));
                    }
                }
        ).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        binding.adminSwitchOrganizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                firebaseManager.grantOrganizer(selectedUser.getUid());
                // Set thumb/track colors for ON state
                binding.adminSwitchOrganizer.setThumbTintList(
                        ContextCompat.getColorStateList(requireContext(), R.color.purple_500)
                );
                binding.adminSwitchOrganizer.setTrackTintList(
                        ContextCompat.getColorStateList(requireContext(), R.color.purple_200)
                );
            } else {
                firebaseManager.revokeOrganizer(selectedUser.getUid());
                Query queryEvents = firebaseManager.getEventsReference().whereEqualTo("Organizer", selectedUser.getUid());
                firebaseManager.getQuery(null, queryEvents, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
                    @Override
                    public void onSuccess(ArrayList<DocumentSnapshot> result) {
                        for (DocumentSnapshot doc : result) {
                            firebaseManager.deleteEvent(doc.getId());
                        }
                        adminViewModel.setEvents(null); // reset events in view model to refresh
                        events.clear();
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AdminUserDetailFragment", "Error deleting events: " + e.getMessage());
                    }
                });
                // TODO: Notify users related to event
                binding.adminSwitchOrganizer.setThumbTintList(ContextCompat.getColorStateList(

                        requireContext(), R.color.grey));
                binding.adminSwitchOrganizer.setTrackTintList(ContextCompat.getColorStateList(

                        requireContext(), R.color.light_grey));
            }
        });


        // Set delete user button
        binding.adminDeleteUserButton.setOnClickListener(view -> {
            // Delete user from database
            firebaseManager.deleteUser(selectedUser.getUid());
            Log.d("Admin delete user", selectedUser.getUid());

            // Remove user from view model
            ArrayList<GeneralUser> users = adminViewModel.getUsers().getValue();
            String removeId = selectedUser.getUid();
            users.removeIf(u -> u.getUid().equals(removeId));
            // reset users in view model to refresh
            adminViewModel.setEvents(null);

            //TODO: send a notification to any users who were registered to event user organized
            Navigation.findNavController(view).popBackStack();
        });

        return root;
    }

}
