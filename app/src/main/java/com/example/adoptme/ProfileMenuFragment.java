package com.example.adoptme;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class ProfileMenuFragment extends Fragment {

    public ProfileMenuFragment() {
        super(R.layout.fragment_profile_menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnPersonalDetails).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profileMenu_to_personalDetails)
        );

        view.findViewById(R.id.btnPreferences).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profileMenu_to_preferences)
        );

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // TODO: your logout logic
        });

        // Optional: bottom nav clicks (works once you add destinations for Home/Profile/Chat/Favorites)
        BottomNavHelper.wire(view, this);
    }
}
