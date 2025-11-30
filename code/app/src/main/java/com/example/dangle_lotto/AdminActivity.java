package com.example.dangle_lotto;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.dangle_lotto.databinding.ActivityAdminBinding;
import com.example.dangle_lotto.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

/**
 * AdminAcitivty — Activity for admin users.
 *<p>
 *     Binds the logout and navigation
 *</p>
 * @author Annie Ding, Mahd Afzal
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize binding
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize and view model
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Get user from firebase and save in view model
        String uid = getIntent().getStringExtra("UID");
        userViewModel.loadUser(uid);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_admin);

        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Clicking bottom nav button opens that fragment and pops fragments off of it
        binding.navView.setOnItemSelectedListener(item -> {
            navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
            NavigationUI.onNavDestinationSelected(item, navController);
            return true;
        });

        // Logout button
        binding.adminLogoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        });
    }
}
