package com.example.dangle_lotto.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.databinding.FragmentAdminViewUsersBinding;

import java.util.ArrayList;
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
    public ArrayList<User> users;
    private RecyclerView recyclerView;
    private UserCardAdapter adapter;
    private LinearLayoutManager manager;
    private User selectedUser;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        binding = FragmentAdminViewUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerView = binding.adminUsersList;
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        users = new ArrayList<>();
        adapter = new UserCardAdapter(users, position -> {
            firebaseManager.getUser(users.get(position).getUid(), new FirebaseCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    selectedUser = result;
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Firebase", "failed to fetch selected user", e);
                }
            });
        });
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // only check when scrolling down
                if (dy <= 0) return;

                int visibleItemCount = manager.getChildCount();
                int totalItemCount = manager.getItemCount();
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
            }
        });

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
                                users.add(user);
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
    public void onDestroyView(){
        super.onDestroyView();
        binding= null;
    }
}

