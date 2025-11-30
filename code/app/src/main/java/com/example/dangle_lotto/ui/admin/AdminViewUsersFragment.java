package com.example.dangle_lotto.ui.admin;

import android.app.AlertDialog;
import android.health.connect.GetMedicalDataSourcesRequest;
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

import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.GeneralUser;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.databinding.FragmentAdminViewUsersBinding;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AdminViewUsersFragment - Fragment displays all user profiles
 * <p>
 * Admins can remove organizer privileges from users and delete accounts.
 * </p>
 *
 * @author Annie Ding
 * @version 1.-
 * @since 2025-11-22
 */
public class AdminViewUsersFragment extends Fragment {
    private FragmentAdminViewUsersBinding binding;
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    public ArrayList<GeneralUser> users;
    private RecyclerView recyclerView;
    private UserCardAdapter adapter;
    private LinearLayoutManager manager;
    private AdminViewModel adminViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        // Inflate the layout for this fragment
        binding = FragmentAdminViewUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up recycler view
        recyclerView = binding.adminUsersList;
        adminViewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        users = new ArrayList<>();

        if (adminViewModel.getUsers().getValue() != null) {
            users = adminViewModel.getUsers().getValue();
        } else {
            users = new ArrayList<>();
        }


        // Set up adapter and attach it to the recycler view
        adapter = new UserCardAdapter(users, position -> {

            adminViewModel.setSelectedUser(users.get(position));
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_adminviewUsers_to_userdetailfragment);
//            GeneralUser user = users.get(position);
//
//            // Show dialog to confirm action
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//
//            // Title
//            builder.setTitle("Manage " + user.getName());
//
//            // Organizer button
//            if (user.canOrganize()) {
//                builder.setPositiveButton("Remove organizer", (dialogInterface, i) -> {
//                    Toast.makeText(getContext(), "Removed organization rights from" + user.getName(), Toast.LENGTH_SHORT).show();
//                    // TODO: Notify user
//
//                    // Remove organizer privilege
//                    user.setCanNotOrganize();
//                });
//            }
//
//            // User button button
//            builder.setNegativeButton("Delete user", (dialogInterface, i) -> {
//                Toast.makeText(getContext(), "Removed user " + user.getName(), Toast.LENGTH_SHORT).show();
//                firebaseManager.deleteUser(user.getUid());
//
//                // Remove user from list
//                users.remove(user);
//                adapter.notifyItemRemoved(i);
//                adapter.notifyItemRangeChanged(i, users.size() - i);
//            });
//
//            // Cancel button
//            builder.setNeutralButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
//            builder.show();
        });
        recyclerView.setAdapter(adapter);

        firebaseManager.getAllUsers(new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                // Clear existing users first
                users.clear();

                // Counter to track when all user fetches are complete
                final int totalUsers = result.size();
                final AtomicInteger loadedCount = new AtomicInteger(0);

                for (String id : result) {
                    firebaseManager.getUser(id, new FirebaseCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            if (!user.isAdmin()) {
                                users.add((GeneralUser) user);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("Firebase", "failed to fetch user: " + id, e);
                        }
                    });
                }

                // Handle case where there are no users
                if (result.isEmpty()) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firebase", "failed to fetch all users", e);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
