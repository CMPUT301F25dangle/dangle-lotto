package com.example.dangle_lotto.ui.admin;

import android.content.Intent;
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
import com.example.dangle_lotto.LoginActivity;
import com.example.dangle_lotto.Notification;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentAdminViewNotificationsBinding;
import com.example.dangle_lotto.ui.NotificationCardAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

/**
 * AdminViewNotificationsFragment - Fragment displays all notifications sent
 * to users.
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-22
 */
public class AdminViewNotificationsFragment extends Fragment {
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private FragmentAdminViewNotificationsBinding binding;
    private RecyclerView recyclerView;
    private NotificationCardAdapter adapter;
    private LinearLayoutManager manager;
    private ArrayList<Notification> notifications;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        // Inflate the layout for this fragment
        binding = FragmentAdminViewNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup recycler view
        recyclerView = root.findViewById(R.id.admin_notifications_list);
        notifications = new ArrayList<>();

        // Setup adapter and attach it to the recycler view
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter = new NotificationCardAdapter(notifications, "AdminView");
        recyclerView.setAdapter(adapter);


        // Fetch all notifications
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collectionGroup("Notifications");
        firebaseManager.getQuery(null, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
            @Override
            public void onSuccess(ArrayList<DocumentSnapshot> result) {
                for (DocumentSnapshot doc: result){
                    Notification notification = firebaseManager.notiDocToNoti(doc);
                    notifications.add(notification);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("AdminViewNotificationsFragment", "Failed to get notifications");
            }
        });

        // Logout button
        binding.adminLogoutBtnNotif.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return root;
    }

    /**
     * Called when the fragment is no longer in use.
     */
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
    }
}