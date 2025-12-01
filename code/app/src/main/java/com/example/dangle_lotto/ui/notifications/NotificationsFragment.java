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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

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

        // get current user object from viewmodel
        user = userViewModel.getUser().getValue();

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initialize listview and firebase manager
        notificationListView = root.findViewById(R.id.notification_lv);
        firebaseManager = FirebaseManager.getInstance();

        // list of all notifications related to a user
        List<Notification> totalNotifications = new ArrayList<>();

        // set adapter for notification list view
        NotificationAdapter adapter = new NotificationAdapter(requireContext(), totalNotifications);
        notificationListView.setAdapter(adapter);

        TextView tvOptedOutMessage = root.findViewById(R.id.tvOptedOutMessage);

        if (user != null && user.getNotiStatus()) {
            tvOptedOutMessage.setVisibility(View.GONE);
            loadNotifications();
        }
        if (user!= null && !user.getNotiStatus()){
            totalNotifications.clear();
            adapter.notifyDataSetChanged();
            tvOptedOutMessage.setVisibility(View.VISIBLE);
        }

        return root;
    }

    /**
     * Loads notifications for the current user and displays them in the ListView.
     * For each notification document, this method fetches the related event to
     * attach its name before updating the UI.
     *
     * Workflow:
     * 1. Fetch all notification documents for the user.
     * 2. For each notification, get the linked event using its eid.
     * 3. Create Notification objects and refresh the adapter as data arrives.
     *
     */
    private void loadNotifications() {
        if (user == null) return;

        firebaseManager.getNotificationsForUser(user.getUid(), new FirebaseCallback<List<DocumentSnapshot>>() {
            @Override
            public void onSuccess(List<DocumentSnapshot> notifDocs) {
                List<Notification> totalNotifications = new ArrayList<>();
                NotificationAdapter adapter = new NotificationAdapter(requireContext(), totalNotifications);
                notificationListView.setAdapter(adapter);

                for (DocumentSnapshot notifDoc : notifDocs) {
                    // Extract fields from the notification document
//                    String senderId = notifDoc.getString("senderId");
//                    String receiverId = notifDoc.getString("receiverId");
//                    String message = notifDoc.getString("message");
//                    Boolean isFromAdmin = notifDoc.getBoolean("isFromAdmin");
//                    String nid = notifDoc.getId();
//                    Timestamp receiptTime = notifDoc.getTimestamp("receiptTime");
//
//                    Notification notification = new Notification(
//                            senderId,
//                            receiverId,
//                            nid,
//                            receiptTime,
//                            message,
//                            isFromAdmin
//                    );
                    Notification notification = firebaseManager.notiDocToNoti(notifDoc);
                    totalNotifications.add(notification);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("NotificationsFragment", "Failed to fetch notifications", e);
            }
        });
    }

    /**
     * eventGrabber - Retrieves an event name from Firestore using the event ID and creates a Notification.
     *
     * @param eid           ID of the event to retrieve
     * @param firebaseManager  FirebaseManager instance used to access Firestore
     * @param status        status label to assign to the notification (ex: Chosen/Not Chosen)
     * @param receiptTime   timestamp of when the notification was received
     * @param callback      callback to handle success or failure of retrieving the event
     */
    public void eventGrabber(String eid, FirebaseManager firebaseManager, String status, com.google.firebase.Timestamp receiptTime, FirebaseCallback<Notification> callback) {
        firebaseManager.getEvent(eid, new FirebaseCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                // Create notification using event name, status, and receipt time
                Notification notification = new Notification(
                        null, // nid can be null if not used here
                        event.getName(), // use event name instead of eid
                        status,
                        receiptTime,
                        null,
                        false
                );
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