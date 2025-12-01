package com.example.dangle_lotto.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.Notification;
import com.example.dangle_lotto.R;
import com.google.firebase.Timestamp;

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
    private static String tag;

    public NotificationCardAdapter(ArrayList<Notification> notifications, String tag) {
        this.tag = tag;
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
         * @param view The root view for the event card.
         */
        public ViewHolder(View view) {
            super(view);
            sender = (TextView) view.findViewById(R.id.admin_notif_sender);
            receiver = (TextView) view.findViewById(R.id.admin_notif_receiver);
            content = (TextView) view.findViewById(R.id.admin_notif_content);
            time = (TextView) view.findViewById(R.id.admin_notif_time);
        }

        public void bind(Notification notification) {
            // Admin view notification
            if (Objects.equals(tag, "AdminView")) {
                // Set the sender text
                if (notification.getIsFromAdmin()) {
                    sender.setText("Admin");
                } else {
                    Log.d("AdminViewNotificationsFragment", "Notification: " + notification.getSenderName());
                    sender.setText(notification.getSenderName());
                }
                // Set the receiver text
                receiver.setText(notification.getReceiverName());
            }

            // User view notification
            if (Objects.equals(tag, "UserView")) {
                sender.setText(null);
                receiver.setText(null);
            }

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