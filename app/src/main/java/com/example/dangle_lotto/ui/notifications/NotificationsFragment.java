package com.example.dangle_lotto.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.Notification;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private ListView notificationListView;
    private FirebaseManager firebaseManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        notificationListView = binding.notificationLv;
        firebaseManager = new FirebaseManager();

        List<Notification> notifications = new ArrayList<>();


        NotificationAdapter adapter = new NotificationAdapter(requireContext(), notifications);
        notificationListView.setAdapter(adapter);



        eventGrabber("7rHZzxyUqLwcju31Rxe8", firebaseManager, "Sex", new FirebaseCallback<Notification>() {
            @Override
            public void onSuccess(Notification notification) {
                notifications.add(notification);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("NotificationsFragment", "Failed to get notification", e);
            }
        });


        return root;
    }

    public void eventGrabber(String eid, FirebaseManager firebaseManager, String eventStatus, FirebaseCallback<Notification> callback) {
        firebaseManager.getEvent(eid, new FirebaseCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                String eventName = event.getName();
                Notification notification = new Notification(eventName, eventStatus);
                callback.onSuccess(notification);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }



    public Notification notiCreater(String name, String status) {
        Notification notification = new Notification(name, status);
        return notification;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}