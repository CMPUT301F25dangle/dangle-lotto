package com.example.dangle_lotto.ui.notifications;

import android.content.Context;
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


public class NotificationAdapter extends ArrayAdapter<Notification> {
    public NotificationAdapter(@NonNull Context context, @NonNull List<Notification> notifications) {
        super(context, 0, notifications);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Notification notification = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notification,
                    parent, false);
        }

        TextView eid = convertView.findViewById(R.id.notification_name);
        TextView status = convertView.findViewById(R.id.notification_status);

        eid.setText(notification.getName());
        status.setText(notification.getStatus());

        return convertView;

    }
}
