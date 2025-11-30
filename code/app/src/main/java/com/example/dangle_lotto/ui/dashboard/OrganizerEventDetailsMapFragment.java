package com.example.dangle_lotto.ui.dashboard;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.FirebaseCallback;
import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.GeneralUser;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.User;
import com.example.dangle_lotto.UserViewModel;
import com.example.dangle_lotto.databinding.FragmentOrganizerEventDetailsMapBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashSet;
import java.util.Set;


/**
 * This fragment opens inside of the OrganizerEventDetails fragment to display the map of users
 */
public class OrganizerEventDetailsMapFragment extends Fragment {
    private FragmentOrganizerEventDetailsMapBinding binding;
    private UserViewModel userViewModel;
    private Event event;

    private MapView map_view;

    // Firebase
    private FirebaseManager firebaseManager;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerEventDetailsMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // initializing view model
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        event = userViewModel.getSelectedOrganizedEvent().getValue();

        // Firebase
        firebaseManager = FirebaseManager.getInstance();

        // osmdroid setup
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        map_view = binding.mapView;
        map_view.setMultiTouchControls(true);

        GeoPoint default_point = new GeoPoint(53.5461, -113.4938);

        map_view.getController().setZoom(11.0);
        map_view.getController().setCenter(default_point);

        loadUserMarkers();


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

    /**
     *  Load location for all users in Registered list for event
     *  Then adds a location marker
     */

    private void loadUserMarkers(){

        if (event == null || map_view == null){return;}

        // Show loading text
        binding.mapOverlayText.setText("Loading entrants on map...");
        binding.mapOverlayText.setVisibility(View.VISIBLE);

        // Collect all unique user IDs you want to show
        Set<String> userIds = new HashSet<>();
        if (event.getRegistered() != null) {
            userIds.addAll(event.getRegistered());
        }
        // need people from chosen list
        if (event.getChosen() != null){
            userIds.addAll(event.getChosen());
        }

        if (userIds.isEmpty()) {
            binding.mapOverlayText.setText("No entrants with map locations yet.");
            return;
        }

        // Clear previous markers
        map_view.getOverlays().clear();

        final int total = userIds.size();
        final int[] done = {0};
        final boolean[] firstMarkerCentered = {false};

        for (String uid : userIds) {
            firebaseManager.getUser(uid, new FirebaseCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (binding == null || map_view == null) {
                        return; // fragment destroyed
                    }

                    if (user instanceof GeneralUser) {
                        GeneralUser gu = (GeneralUser) user;
                        if (gu.getLocation() != null) {
                            GeoPoint p = new GeoPoint(
                                    gu.getLocation().getLatitude(),
                                    gu.getLocation().getLongitude()
                            );
                            addMarkerForUser(gu, p);

                            // Center map on the first valid user location
                            if (!firstMarkerCentered[0]) {
                                map_view.getController().setCenter(p);
                                firstMarkerCentered[0] = true;
                            }
                        }
                    }
                    checkFinished();
                }

                @Override
                public void onFailure(Exception e) {
                    checkFinished();
                }

                private void checkFinished() {
                    done[0]++;
                    if (done[0] == total && binding != null && map_view != null) {

                        if (!firstMarkerCentered[0]){
                            // Did not find user with valid location
                            binding.mapOverlayText.setText("No users have location data yet");
                            binding.mapOverlayText.setVisibility(View.VISIBLE);
                        }
                        else {
                            // At least one marker added and centered, then hide overlay
                            binding.mapOverlayText.setVisibility(View.GONE);
                        }
                        map_view.invalidate();
                    }
                }
            });
        }



    }

    /**
     * Adds a marker for a given GeneralUser at the given GeoPoint.
     */
    private void addMarkerForUser(GeneralUser user, GeoPoint point) {
        if (map_view == null || user == null || point == null) return;

        Marker marker = new Marker(map_view);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Title inside the bubble when you tap the marker
        marker.setTitle(user.getUsername());

        map_view.getOverlays().add(marker);
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