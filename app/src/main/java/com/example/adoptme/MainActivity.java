package com.example.adoptme;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();

        navView = findViewById(R.id.bottom_navigation);
        navView.setItemIconTintList(null);

        NavigationUI.setupWithNavController(navView, navController);

        // Hide BottomNav on auth screens
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();

            boolean hide =
                    id == R.id.homeFragment ||
                            id == R.id.loginFragment ||
                            id == R.id.registerFragment ||
                            id == R.id.matchSuccessScreen; // optional, לפי הפיגמה שלך זה מסך בלי bottom bar

            navView.setVisibility(hide ? View.GONE : View.VISIBLE);
        });
    }
}
