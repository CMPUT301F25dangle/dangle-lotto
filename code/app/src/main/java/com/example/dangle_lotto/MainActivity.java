package com.example.dangle_lotto;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.dangle_lotto.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;

/**
 * MainActivity - Main activity for the app.
 *
 * Requests user location once on startup and updates it in firebase.
 *
 * @author Mahd Afzal, Aditya Soni
 * @version 2.0
 * @since 2025-10-25
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserViewModel userViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    /** Permission request dialog launcher. */
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getUserLocation();
                } else {
                    Toast.makeText(this, "Some events require location permission to register.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize and view model
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Get user from firebase and save in view model
        String uid = getIntent().getStringExtra("UID");
        userViewModel.loadUser(uid);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_your_events, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Clicking bottom nav button opens that fragment and pops fragments off of it
        binding.navView.setOnItemSelectedListener(item -> {
            navController.popBackStack(item.getItemId(), false);
            NavigationUI.onNavDestinationSelected(item, navController);
            return true;
        });

        // get location provider client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ask for permission when activity begins after the user is loaded into the viewmodel
        userViewModel.getUser().observe(this, user -> {
            if (user != null) {
                // Now that user is loaded from Firestore, request location
                checkLocationPermission();
            }
        });

    }

    /**
     * Checks if the app has location permission. If not, requests it.
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            // Should we show a rationale?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showLocationRationaleDialog();
            } else {
                // Directly request permission
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    /**
     * Shows a dialog to explain why location permission is needed.
     */
    private void showLocationRationaleDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Location Permission Recommended")
                .setMessage("Some events require location permission to register. If location is not provided, you won't be able to register for events that require location.")
                .setPositiveButton("Allow", (dialog, which) -> {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Gets the user's current location and updates the loaded user in the view model and
     * corresponding entry in firebase.
     */
    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        Log.d("Location", "Lat: " + lat + ", Lng: " + lng);

                        GeoPoint point = new GeoPoint(lat, lng);
                        Objects.requireNonNull(userViewModel.getUser().getValue()).setLocation(point);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Location", "Error getting location", e);
                    Toast.makeText(this,
                            "Error getting location. You won't be able to register for events that require location.",
                            Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(userViewModel.getUser().getValue()).setLocation(null);

                });
    }

    @Override
    protected void onDestroy() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean remember = prefs.getBoolean("rememberMe", true);
        Log.d("onDestroy", "rememberMe: " + remember);
        if (!remember) {
            Log.d("onDestroy", "rememberMe is firing: " + remember);
            FirebaseManager.getInstance().getAuth().signOut();
        }

        super.onDestroy();
    }

}