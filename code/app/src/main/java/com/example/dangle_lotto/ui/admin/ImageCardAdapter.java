package com.example.dangle_lotto.ui.admin;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.R;

import java.util.ArrayList;

/**
 * ImageCardAdapter - Custom card adapter to display images
 * <p>
 *     Admins can see all images and click on the image to select it
 * </p>
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-29
 */
public class ImageCardAdapter extends RecyclerView.Adapter<ImageCardAdapter.ViewHolder> {
    private ArrayList<String> images = new ArrayList<>();
    private final OnItemClickListener listener;
    private static final FirebaseManager firebaseManager = FirebaseManager.getInstance();

    /**
     * Interface for handling item click events in the adapter.
     *
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * Constructor for ImageCardAdapter.
     *
     * @param images
     * @param listener
     */
    public ImageCardAdapter(ArrayList<String> images, OnItemClickListener listener){
        this.images = images;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder that holds references to the views within an event card.
     *
     * @param viewGroup   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_image, viewGroup, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param viewHolder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position){
        viewHolder.bind(images.get(position), listener);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return
     */
    @Override
    public int getItemCount(){
        return images.size();
    }

    /**
     * ViewHolder class for holding references to the views within an event card.
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView imageView;

        public ViewHolder(View view){
            super(view);
            imageView = (ImageView)  view.findViewById(R.id.admin_manage_image);
        }
        public void bind(String image, OnItemClickListener listener){
            Glide.with(imageView.getContext()).load(image).into(imageView);
            itemView.setOnClickListener(v -> listener.onItemClick(getBindingAdapterPosition()));

        }
    }
}