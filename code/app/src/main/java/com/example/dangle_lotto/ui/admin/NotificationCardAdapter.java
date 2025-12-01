package com.example.dangle_lotto.ui.admin;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.Notification;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * NotificationCardAdapter - Custom notification card adapter to be used to display notifications
 * for admins to manage.
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-12-01
 */
public class NotificationCardAdapter extends RecyclerView.Adapter<NotificationCardAdapter.ViewHolder> {
    private static ArrayList<Notification> notifications = new ArrayList<>();

    private static final FirebaseManager firebaseManager = FirebaseManager.getInstance();

    public NotificationCardAdapter(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }

    @Nonnull
    @Override
    public ViewHolder onCreateViewHolder(@Nonnull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_notification, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@Nonnull ViewHolder viewHolder, int position) {
        viewHolder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView sender;
        private final TextView receiver;
        private final TextView content;
        private final TextView time;

        /**
         * Creates a new ViewHolder that holds references to the views within an event card.
         *
         * @param view
         */
        public ViewHolder(View view) {
            super(view);
            sender = (TextView) view.findViewById(R.id.admin_notif_sender);
            receiver = (TextView) view.findViewById(R.id.admin_notif_receiver);
            content = (TextView) view.findViewById(R.id.admin_notif_content);
            time = (TextView) view.findViewById(R.id.admin_notif_time);
        }

        public void bind(Notification notification) {
            // USe firebase to get the sender and receiver
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query query = db.collectionGroup("Notifications").whereEqualTo("nid", notification.getNid());
            firebaseManager.getQuery(null, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
                @Override
                public void onSuccess(ArrayList<DocumentSnapshot> result) {
                    if (result.isEmpty()) {
                        Log.d("NotificationCardAdapter", "Failed to get receiver");
                        return;
                    }
                    DocumentSnapshot doc = result.get(0);
                    firebaseManager.getUser(doc.getReference().getParent().getParent().getId(), new FirebaseCallback<User>() {
                        @Override
                        public void onSuccess(User result) {
                            receiver.setText("To: " + result.getName());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.d("NotificationCardAdapter", "Failed to get receiver");
                        }
                    });

                }

                @Override
                public void onFailure(Exception e) {
                    Log.d("NotificationCardAdapter", "Failed to get sender");
                }
            });
            firebaseManager.getEvent(notification.getEid(), new FirebaseCallback<Event>() {
                @Override
                public void onSuccess(Event result) {
                    firebaseManager.getUser(result.getOrganizerID(), new FirebaseCallback<User>() {
                        @Override
                        public void onSuccess(User result) {
                            sender.setText("From: " + result.getName());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.d("NotificationCardAdapter", "Failed to get sender");
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d("NotificationCardAdapter", "Failed to get event");
                }
            });
            content.setText(notification.getMessage());
            time.setText(formatTimeStamp(notification.getReceiptTime()));
        }

        /**
         * Formats the time
         *
         * @param ts Timestamp
         * @return String
         */
        private String formatTimeStamp(Timestamp ts) {
            if (ts == null) return "N/A";
            Date date = ts.toDate();
            DateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return df.format(date);
        }
    }
}