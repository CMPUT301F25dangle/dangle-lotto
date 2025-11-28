package com.example.dangle_lotto.ui.admin;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.ui.EventCardAdapter;

import java.util.ArrayList;

import javax.annotation.Nonnull;

/**
 * UserCardAdapter - Custom user card adapter to be used to display users
 * for admins to manage.
 * <p>
 * Users will be represented as a card with their profile picture, name, and email.
 * </p>
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-27
 */
public class UserCardAdapter extends RecyclerView.Adapter<UserCardAdapter.ViewHolder> {
    private static ArrayList<User> users = new ArrayList<>();
    private final OnItemClickListener listener;

    private static final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public UserCardAdapter(ArrayList<User> Users, OnItemClickListener listener) {
        users = Users;
        this.listener = listener;
    }
    @Nonnull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_user, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.bind(users.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView name;
        private final TextView email;

        /**
         * Creates a new ViewHolder that holds references to the views within an event card.
         *
         * @param view The inflated layout view representing a single event card item.
         */
        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.admin_picture_location);
            name = (TextView) view.findViewById(R.id.admin_user_name);
            email = (TextView) view.findViewById(R.id.admin_user_email);
        }

        public void bind(User user, OnItemClickListener listener) {
            name.setText(user.getName());
            email.setText(user.getEmail());
            if (user.getPhotoID() == null || user.getPhotoID().isEmpty()) {
                imageView.setImageResource(R.drawable.default_profile_picture);
            } else {
                Glide.with(imageView.getContext()).load(user.getPhotoID()).into(imageView);
            }
            itemView.setOnClickListener(v -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Manage " + user.getName());
                builder.setPositiveButton("Remove organizer", (dialogInterface, i) -> {
                    Toast.makeText(v.getContext(), "Removed organization rights from" + user.getName(), Toast.LENGTH_SHORT).show();
                    // TODO: Notify user
                    firebaseManager.revokeOrganizer(user.getUid());
                    listener.onItemClick(getBindingAdapterPosition());
                });
                builder.setNegativeButton("Delete user ", (dialogInterface, i) -> {
                    Toast.makeText(v.getContext(), "Removed user " + user.getName(), Toast.LENGTH_SHORT).show();
                    firebaseManager.deleteUser(user.getUid());
                });
                builder.setNeutralButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
                builder.show();
            });
        }

    }
}