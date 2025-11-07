package com.example.dangle_lotto.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.GeneralUser;
import com.example.dangle_lotto.Notification;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private UserViewModel userViewModel;

    private GeneralUser user;

    private FragmentNotificationsBinding binding;
    private ListView notificationListView;
    private FirebaseManager firebaseManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initializing view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initialize listview and firebase manager
        notificationListView = binding.notificationLv;
        firebaseManager = new FirebaseManager();

        // list of all notifications related to a user
        List<Notification> totalNotifications = new ArrayList<>();

        // set adapter for notification list view
        NotificationAdapter adapter = new NotificationAdapter(requireContext(), totalNotifications);
        notificationListView.setAdapter(adapter);

        // get current user object from viewmodel
        user = userViewModel.getUser().getValue();

        ArrayList<String> chosenEventsList = user.chosenEvents();

        Log.d("NotificationsFragment", "User chosen events: " + chosenEventsList);


        for (String eid : chosenEventsList) {
            eventGrabber(eid, firebaseManager, "Chosen", new FirebaseCallback<Notification>() {
                @Override
                public void onSuccess(Notification notification) {
                    totalNotifications.add(notification);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("NotificationsFragment", "Failed to get notification", e);
                }
            });
        }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}