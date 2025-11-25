package com.example.dangle_lotto.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.databinding.FragmentAdminViewImagesBinding;

/**
 * AdminViewImagesFragment - This fragment displays all images
 * uploaded to the app.
 * <p>
 *     Admin users can select and remove images.
 * </p>
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-22
 */
public class AdminViewImagesFragment extends Fragment {
    private final FirebaseManager firebaseManager = FirebaseManager.getInstance();
    private FragmentAdminViewImagesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentAdminViewImagesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
}
