package com.example.adoptme;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }


        ImageButton btnChat = findViewById(R.id.navChat);
        ImageButton btnFavorites = findViewById(R.id.navFavorites);
        ImageButton btnHome = findViewById(R.id.navHome);
        ImageButton btnProfile = findViewById(R.id.navProfile);


        btnProfile.setOnClickListener(v -> {
            navController.navigate(R.id.profileMenuFragment);
        });

        btnFavorites.setOnClickListener(v -> {
            navController.navigate(R.id.favoritesFragment);
        });

        btnChat.setOnClickListener(v -> {
            navController.navigate(R.id.chatListFragment);
        });

        btnHome.setOnClickListener(v -> {
            // הניווט למסך ה-Explor
            navController.navigate(R.id.explorFragment);
        });


        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            updateBottomNavIcons(destination.getId());
        });
    }

    private void updateBottomNavIcons(int destinationId) {
        ImageButton btnChat = findViewById(R.id.navChat);
        ImageButton btnFavorites = findViewById(R.id.navFavorites);
        ImageButton btnHome = findViewById(R.id.navHome);
        ImageButton btnProfile = findViewById(R.id.navProfile);


        btnChat.setAlpha(0.5f);
        btnFavorites.setAlpha(0.5f);
        btnHome.setAlpha(0.5f);
        btnProfile.setAlpha(0.5f);


        if (destinationId == R.id.chatListFragment) {
            btnChat.setAlpha(1.0f);
        } else if (destinationId == R.id.favoritesFragment) {
            btnFavorites.setAlpha(1.0f);
        } else if (destinationId == R.id.profileMenuFragment) {
            btnProfile.setAlpha(1.0f);
        }

    }
}


