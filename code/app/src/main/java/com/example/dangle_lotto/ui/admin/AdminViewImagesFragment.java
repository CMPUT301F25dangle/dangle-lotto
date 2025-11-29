package com.example.dangle_lotto.ui.admin;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.databinding.FragmentAdminViewImagesBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

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
    private RecyclerView recyclerView;
    private ImageCardAdapter adapter;
    private LinearLayoutManager manager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminViewImagesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerView = binding.adminImagesRecyclerView;
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        images = new ArrayList<>();
        adapter = new ImageCardAdapter(images, position -> {
            Query query = firebaseManager.getEventsReference().whereNotEqualTo("Picture", null);
            firebaseManager.getQuery(null, query, new FirebaseCallback<ArrayList<DocumentSnapshot>>() {
                @Override
                public void onSuccess(ArrayList<DocumentSnapshot> result) {
                    int startPos = images.size();
                    for (DocumentSnapshot doc : result) {
                        images.add(firebaseManager.documentToEvent(doc).getPhotoID());
                    }
                    adapter.notifyItemRangeInserted(startPos, images.size());
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d("Firebase", "Failed to load iamges", e);
                }
            });
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // only check when scrolling down
                if (dy <= 0) return;

                int visibleItemCount = manager.getChildCount();
                int totalItemCount = manager.getItemCount();
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
            }
        });
        return root;
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        binding= null;
    }
}