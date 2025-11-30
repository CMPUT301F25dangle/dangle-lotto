package com.example.dangle_lotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.Notification;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentAdminViewNotificationsBinding;
import com.example.dangle_lotto.ui.notifications.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

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
    private RecyclerView notificationListView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        binding = FragmentAdminViewNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        notificationListView = root.findViewById(R.id.admin_notifications_list);
        List<Notification> totalNotifications = new ArrayList<>();
        NotificationAdapter adapter = new NotificationAdapter(requireContext(), totalNotifications);
//        notificationListView.setAdapter(adapter);

        return root;
    }

}