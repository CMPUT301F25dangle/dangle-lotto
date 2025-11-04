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

/**
 * EventCardAdapter - Custom event card adapter to be used when you want to display events in a recycler view.
 * <p>
 * Event objects will be each represented as a card with an image and title, which can be clicked.
 * Implement your own on click listener.
 *
 * <h4>Example Usage</h4>
 * <pre><code>
 * EventCardAdapter eventCardAdapter = new EventCardAdapter(Events, position -> {
 *   Event clickedEvent = Events.get(position);
 *   Toast.makeText(context, "Clicked: " + clickedEvent.getTitle(), Toast.LENGTH_SHORT).show();
 * })
 * </code></pre>
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 2025-10-25
 */
public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.ViewHolder> {
    private final ArrayList<Event> Events;
    private final OnItemClickListener listener;

    /**
     * Interface for handling clicks on event cards.
     */
    public interface OnItemClickListener {
        /**
         * Called when an event card is clicked.
         *
         * @param position The adapter position of the clicked item.
         */
        void onItemClick(int position);
    }

    /**
     * Creates a new EventCardAdapter.
     *
     * @param Events The arraylist of Event objects to display.
     * @param listener Listener for handling click events on individual items.
     */
    public EventCardAdapter(ArrayList<Event> Events, OnItemClickListener listener) {
        this.Events = Events;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder.
     *
     * @param viewGroup The parent view that the new view will be added to.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder instance containing the inflated layout.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_card, viewGroup, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data for a specific position in the dataset to the given {@link ViewHolder}.
     *
     * @param viewHolder The ViewHolder to bind data to.
     * @param position The position of the item within the dataset.
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.bind(Events.get(position), listener);
    }

    /**
     * Returns the number of events in the dataset.
     *
     * @return The size of the Events list, or 0 if null.
     */
    @Override
    public int getItemCount() {
        return Events == null ? 0 : Events.size();
    }

    /**
     * ViewHolder for an individual event card.
     * <p>
     * Holds references to the image and title views for quick access during binding.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;

        /**
         * Creates a new ViewHolder that holds references to the views within an event card.
         *
         * @param view The inflated layout view representing a single event card item.
         */
        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.event_card_banner);
            textView = (TextView) view.findViewById(R.id.event_card_title);
        }

        /**
         * Populates the view with data from the given Event and sets up click handling.
         *
         * @param event The event data to display.
         * @param listener The click listener to invoke when the item is clicked.
         */
        public void bind(Event event, OnItemClickListener listener) {
            textView.setText(event.getTitle());
            imageView.setImageResource(event.getBanner());
            itemView.setOnClickListener(v -> listener.onItemClick(getBindingAdapterPosition()));
        }
    }
}
