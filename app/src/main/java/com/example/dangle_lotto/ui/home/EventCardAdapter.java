package com.example.dangle_lotto.ui.home;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.ViewHolder> {
    private final ArrayList<Event> Events;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param Events String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public EventCardAdapter(ArrayList<Event> Events, OnItemClickListener listener) {
        this.Events = Events;
        this.listener = listener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_card, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.bind(Events.get(position), listener);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Events == null ? 0 : Events.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.event_card_image);
            textView = (TextView) view.findViewById(R.id.event_card_title);
        }

        public void bind(Event event, OnItemClickListener listener) {
            textView.setText(event.getTitle());
            imageView.setImageResource(event.getImageResId());
            itemView.setOnClickListener(v -> listener.onItemClick(getBindingAdapterPosition()));
        }
    }
}
