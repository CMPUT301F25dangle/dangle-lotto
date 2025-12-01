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
 * This fragment opens inside of the OrganizerEventDetailsMao fragment to
 * display the map of users
 *
 * @author Cainan Kousol-Graham
 * @version 1.0
 * @since 2025-11-30
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

        // Start map with with a focus on the center of Edmonton
        map_view.getController().setZoom(11.0);
        map_view.getController().setCenter(default_point);

        loadUserMarkers();


        return root;
    }


    /**
     *  Load location for all users in Registered list for event
     *  Then adds a location marker
     */

    private void loadUserMarkers(){

        if (event == null || map_view == null){
            return;
        }

        if (event.isLocationRequired() == false){
            binding.mapOverlayText.setText("Geolocation for event is turned off.");
            binding.mapOverlayText.setVisibility(View.VISIBLE);
            map_view.getOverlays().clear();
            map_view.invalidate();
            return;
        }

        // Show loading text
        binding.mapOverlayText.setText("Loading entrants on map...");
        binding.mapOverlayText.setVisibility(View.VISIBLE);

        // Collect all unique user IDs you want to show
        Set<String> userIds = new HashSet<>();

        // Get people from the registered list
        if (event.getRegistered() != null) {
            userIds.addAll(event.getRegistered());
        }
        // need people from chosen list - people chosen from lottery
        if (event.getChosen() != null){
            userIds.addAll(event.getChosen());
        }

        // need people from Sign up list - people that accepted
        if (event.getSignUps() != null){
            userIds.addAll(event.getSignUps());
        }

        // if users id is empty we output message to screen
        if (userIds.isEmpty()) {
            binding.mapOverlayText.setText("No entrants with map locations yet.");
            map_view.getOverlays().clear();
            map_view.invalidate();
            return;
        }

        // Clear previous markers
        map_view.getOverlays().clear();

        // Track which users are still being processed
        final Set<String> remainingUserIds = new HashSet<>(userIds);
        final boolean[] hasAnyMarker = { false }; // at least one user had a location
        final boolean[] firstCentered = { false }; // only center on first

        for (String uid : userIds) {
            firebaseManager.getUser(uid, new FirebaseCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (binding == null || map_view == null) {
                        return; // fragment destroyed, bail out
                    }

                    if (user instanceof GeneralUser) {
                        GeneralUser gu = (GeneralUser) user;
                        if (gu.getLocation() != null) {
                            GeoPoint p = new GeoPoint(
                                    gu.getLocation().getLatitude(),
                                    gu.getLocation().getLongitude()
                            );
                            addMarkerForUser(gu, p);
                            hasAnyMarker[0] = true;

                            // Center map on the first valid user location
                            if (!firstCentered[0]) {
                                map_view.getController().setCenter(p);
                                firstCentered[0] = true;
                            }
                        }
                    }
                    checkFinished(uid, remainingUserIds,hasAnyMarker[0]);
                }

                @Override
                public void onFailure(Exception e) {

                    checkFinished(uid, remainingUserIds,hasAnyMarker[0]);
                }

                /**
                 * Tracks completion of asynchronous users-location fetch and updates map overlay
                 *
                 * @param process_id The user ID whose fetch operation has just completed
                 * @param remaining  A set if user IDs still waiting for results. When empty
                 *                   all async user fetches are done.
                 * @param hasMarkerSoFar if at least one user has a valid geolocation and was
                 *                       successfully added to map
                 */
                private void checkFinished(String process_id, Set<String> remaining, boolean hasMarkerSoFar)
                {
                    // only bail if views are GONE
                    if (binding == null || map_view == null) {
                        return;
                    }

                    // Remove this user from the remaining list
                    remaining.remove(process_id);

                    if (remaining.isEmpty()) {
                        // All async calls finished
                        if (hasMarkerSoFar) {
                            // At least one user had a valid location and hide overlay
                            binding.mapOverlayText.setVisibility(View.GONE);

                        } else {
                            // No one had a location
                            binding.mapOverlayText.setText("No entrants with map locations yet.");
                            binding.mapOverlayText.setVisibility(View.VISIBLE);
                        }

                        map_view.invalidate();

                    }
                }
            });
        }

    }


    /**
     * Adds a marker for a given GeneralUser at the given GeoPoint.
     *
     * @param user
     * @param point
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