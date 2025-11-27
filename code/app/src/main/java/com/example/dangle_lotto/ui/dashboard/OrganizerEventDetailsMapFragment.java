package com.example.dangle_lotto.ui.dashboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentOrganizerEventDetailsMapBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import javax.annotation.Nullable;

/**
 * This fragment opens inside of the OrganizerEventDetails fragment to display the map of users
 */
public class OrganizerEventDetailsMapFragment extends Fragment {
    private FragmentOrganizerEventDetailsMapBinding binding;
    private UserViewModel userViewModel;
    private Event event;

    private MapView map_view;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerEventDetailsMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initializing view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        event = userViewModel.getSelectedOrganizedEvent().getValue();

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        map_view = binding.mapView;
        map_view.setMultiTouchControls(true);

        GeoPoint default_point = new GeoPoint(53.5461, -113.4938);
        map_view.getController().setZoom(12.0);
        map_view.getController().setCenter(default_point);

        addTestMarker(default_point);


        return root;
    }

    private void addTestMarker(GeoPoint point) {
        if (map_view == null) return;

        Marker marker = new Marker(map_view);
        marker.setPosition(point);
        marker.setTitle("Event location / test marker");
        map_view.getOverlays().add(marker);
        map_view.invalidate();

        // Hide loading overlay
        binding.mapOverlayText.setVisibility(View.GONE);
    }


    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        if (binding != null) {
            binding.mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        map_view = null;
        binding = null;
    }

}