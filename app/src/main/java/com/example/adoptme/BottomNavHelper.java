package com.example.adoptme;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class BottomNavHelper {

    public static void wire(View root, Fragment fragment) {
        View chat = root.findViewById(R.id.navChat);
        View fav = root.findViewById(R.id.navFavorites);
        View home = root.findViewById(R.id.navHome);
        View profile = root.findViewById(R.id.navProfile);

        NavController nav = NavHostFragment.findNavController(fragment);

        if (chat != null) {
            chat.setOnClickListener(v -> nav.navigate(R.id.chatListFragment));
        }
        if (fav != null) {
            fav.setOnClickListener(v -> nav.navigate(R.id.favoritesFragment));
        }

        // These two need real destinations in your nav_graph:
        if (home != null) {
            home.setOnClickListener(v -> {
                // TODO: change to your real home fragment id
                // nav.navigate(R.id.homeFragment);
            });
        }
        if (profile != null) {
            profile.setOnClickListener(v -> nav.navigate(R.id.profileMenuFragment));
        }
    }
}

