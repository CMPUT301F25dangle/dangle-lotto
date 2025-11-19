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


/**
 * NotificationsFragment - Fragment displays all notifications related to the user.
 * <p>
 * Retrieves events the user has chosen or not chosen from Firestore, converts them
 * into Notification objects, and displays them in a ListView using NotificationAdapter.
 *
 * @author Prem Elango
 * @version 1.0
 * @since 2025-11-05
 */

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
        notificationListView = root.findViewById(R.id.notification_lv);
        firebaseManager = new FirebaseManager();

        // list of all notifications related to a user
        List<Notification> totalNotifications = new ArrayList<>();

        // set adapter for notification list view
        NotificationAdapter adapter = new NotificationAdapter(requireContext(), totalNotifications);
        notificationListView.setAdapter(adapter);

        // get current user object from viewmodel
        user = userViewModel.getUser().getValue();

        // display notifications for chosen events
        user.chosenEvents(new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> eventIds) {
                for (String eid : eventIds) {
                    eventGrabber(eid, firebaseManager, "Chosen", new FirebaseCallback<Notification>() {
                        @Override
                        public void onSuccess(Notification notification) {
                            requireActivity().runOnUiThread(() -> {
                                totalNotifications.add(notification);
                                adapter.notifyDataSetChanged();
                            });
                        }
                        @Override
                        public void onFailure(Exception e) {
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        });

        // display notifications for registered events
        user.registeredEvents(new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> eventIds) {
                for (String eid : eventIds) {
                    eventGrabber(eid, firebaseManager, "On waiting list (registered)", new FirebaseCallback<Notification>() {
                        @Override
                        public void onSuccess(Notification notification) {
                            requireActivity().runOnUiThread(() -> {
                                totalNotifications.add(notification);
                                adapter.notifyDataSetChanged();
                            });
                        }
                        @Override
                        public void onFailure(Exception e) {
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        });

        // display notifications for user cancelled events
        user.cancelledEvents(new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> eventIds) {
                for (String eid : eventIds) {
                    eventGrabber(eid, firebaseManager, "You have cancelled (declined)", new FirebaseCallback<Notification>() {
                        @Override
                        public void onSuccess(Notification notification) {
                            requireActivity().runOnUiThread(() -> {
                                totalNotifications.add(notification);
                                adapter.notifyDataSetChanged();
                            });
                        }
                        @Override
                        public void onFailure(Exception e) {
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        });


        return root;
    }
    /**
     * eventGrabber - Retrieves an event from Firestore and converts it into a Notification.
     * <p>
     * Given an event ID, this method fetches the event details using FirebaseManager,
     * creates a Notification object with the event name and status, and returns it
     * through the provided callback.
     *
     * @param eid           ID of the event to retrieve
     * @param firebaseManager  FirebaseManager instance used to access Firestore
     * @param eventStatus   status label to assign to the notification (ex: Chosen/ Not Chosen)
     * @param callback      callback to handle success or failure of retrieving the event
     */
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