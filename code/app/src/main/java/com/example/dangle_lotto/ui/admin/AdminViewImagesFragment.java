package com.example.dangle_lotto.ui.admin;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.databinding.FragmentAdminViewImagesBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AdminViewImagesFragment - This fragment displays all images
 * uploaded to the app.
 * <p>
 * Admin users can select and remove images.
 * </p>
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-22
 */
public class AdminViewImagesFragment extends Fragment {
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private FragmentAdminViewImagesBinding binding;
    private ArrayList<String> images;
    private ArrayList<Event> events;
    private RecyclerView recyclerView;
    private ImageCardAdapter adapter;
    AdminViewModel adminViewModel;
    private LinearLayoutManager manager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAdminViewImagesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup recycler view
        recyclerView = binding.adminImagesRecyclerView;
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        images = new ArrayList<>();
        events = new ArrayList<>();

        // Setup view model
        adminViewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        // Setup an adapter and attach it to the recycler view
        adapter = new ImageCardAdapter(images, position -> {
            String image = images.get(position);

            // Show dialog to confirm action
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            // Title
            builder.setTitle("Remove image");
            builder.setMessage("Are you sure you want to remove this image?");

            builder.setPositiveButton("Delete", (dialogInterface, i) -> {
                Toast.makeText(getContext(), "Removed image", Toast.LENGTH_SHORT).show();
                // TODO: Notify user
                firebaseManager.deletePic(image, new FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Remove image from list
                        images.remove(image);
                        adapter.notifyItemRemoved(i);
                        adapter.notifyItemRangeChanged(i, images.size() - i);

                        // Remove image from event
                        for (Event event : events) {
                            if (event.getPhotoID().equals(image)) {
                                event.setPhotoID(null);

                                // resets the event in the view model, so it refetches
                                adminViewModel.setEvents(null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Firebase", "failed to delete image: " + image, e);
                    }
                });
            });

            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
        });
        recyclerView.setAdapter(adapter);

        firebaseManager.getAllEvents(new FirebaseCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                events.clear();
                images.clear();

                for (String id : result) {
                    firebaseManager.getEvent(id, new FirebaseCallback<Event>() {
                        @Override
                        public void onSuccess(Event event) {
                            images.add(event.getPhotoID());
                            events.add(event);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("Firebase", "failed to fetch event: " + id, e);
                        }
                    });
                }
                if (result.isEmpty()) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firebase", "failed to fetch all events", e);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}