package com.example.dangle_lotto;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.dangle_lotto.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseManager firebaseManager;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize firebase manager and user view model
        firebaseManager = new FirebaseManager();
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

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
    }
}