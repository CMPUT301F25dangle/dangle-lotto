package com.example.dangle_lotto.ui.notifications;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dangle_lotto.R;


import com.example.dangle_lotto.Notification;

import java.util.List;

/**
 * NotificationAdapter - Adapter to display a list of notifications.
 * <p>
 * Populates each list item with a notification’s name and status using
 * the item_notification layout.
 *
 * @author Prem Elango
 * @version 1.0
 * @since 2025-11-05
 */

public class NotificationAdapter extends ArrayAdapter<Notification> {


    public NotificationAdapter(@NonNull Context context, @NonNull List<Notification> notifications) {
        super(context, 0, notifications);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_notification, parent, false);
        }

        Notification notification = getItem(position);
        TextView name = convertView.findViewById(R.id.notification_name);
        TextView status = convertView.findViewById(R.id.notification_status);
        TextView time = convertView.findViewById(R.id.notification_time);

        if (notification != null) {
            name.setText(notification.getEid());
            status.setText(notification.getStatus());

            if (notification.getReceipt_time() != null) {
                String formattedTime = android.text.format.DateFormat.format(
                        "dd/MM/yyyy hh:mm a",
                        notification.getReceipt_time().toDate()
                ).toString();
                time.setText(formattedTime);
            } else {
                time.setText("");
            }
        }

        return convertView;
    }




}
