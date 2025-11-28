//package com.example.dangle_lotto.ui.admin;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.dangle_lotto.R;
//import com.example.dangle_lotto.User;
//import com.example.dangle_lotto.ui.EventCardAdapter;
//
//import java.util.ArrayList;
//
//import javax.annotation.Nonnull;
//
///**
// * UserCardAdapter - Custom user card adapter to be used to display users
// * for admins to manage.
// * <p>
// * Users will be represented as a card with their profile picture, name, and email.
// * </p>
// *
// * @author Annie Ding
// * @version 1.0
// * @since 2025-11-27
// */
//public class UserCardAdapter extends RecyclerView.Adapter<UserCardAdapter.ViewHolder> {
//    private final ArrayList<User> Users;
//    private final OnItemClickListener listener;
//
//    public interface OnItemClickListener {
//        void onItemClick(int position);
//    }
//
//    public UserCardAdapter(ArrayList<User> Users, OnItemClickListener listener) {
//        this.Users = Users;
//        this.listener = listener;
//    }
//
//    @Nonnull
//    @Override
//    public EventCardAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_card)
//    }
//}
