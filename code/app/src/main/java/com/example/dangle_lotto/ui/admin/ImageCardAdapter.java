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
import com.example.dangle_lotto.R;

import java.util.ArrayList;

public class ImageCardAdapter extends RecyclerView.Adapter<ImageCardAdapter.ViewHolder> {
    private ArrayList<String> images = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ImageCardAdapter(ArrayList<String> images, OnItemClickListener listener){
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_image, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position){
        viewHolder.bind(images.get(position), listener);
    }

    @Override
    public int getItemCount(){
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView imageView;

        public ViewHolder(View view){
            super(view);
            imageView = (ImageView)  view.findViewById(R.id.admin_manage_image);
        }
        public void bind(String image, OnItemClickListener listener){
            Glide.with(imageView.getContext()).load(image).into(imageView);
        }
    }
}