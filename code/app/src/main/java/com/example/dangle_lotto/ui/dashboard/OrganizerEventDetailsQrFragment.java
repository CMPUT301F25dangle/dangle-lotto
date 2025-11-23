package com.example.dangle_lotto.ui.dashboard;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentOrganizerEventDetailsQrBinding;

public class OrganizerEventDetailsQrFragment extends Fragment {
    private FragmentOrganizerEventDetailsQrBinding binding;
    private UserViewModel userViewModel;
    private Event event;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerEventDetailsQrBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        event = userViewModel.getSelectedOrganizedEvent().getValue();

        ProgressBar spinner = binding.qrLoading;

        spinner.setVisibility(View.VISIBLE);

        Glide.with(requireContext())
                .load(event.getQR())
//                .error(R.drawable.error_qr)              // optional fallback
                .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource) {

                        spinner.setVisibility(View.GONE); // hide on failure
                        return false; // let Glide handle the error placeholder
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource) {

                        spinner.setVisibility(View.GONE); // hide when loaded
                        return false; // let Glide handle setting the image
                    }
                })
                .into(binding.qrView);

        return root;
    }
}
