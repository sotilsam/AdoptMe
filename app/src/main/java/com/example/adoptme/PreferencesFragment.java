package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.slider.Slider;

public class PreferencesFragment extends Fragment {

    public PreferencesFragment() {
        super(R.layout.fragment_preferences);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView title = view.findViewById(R.id.tvTitle);
        title.setText("Preferences");

        ImageButton back = view.findViewById(R.id.btnBack);
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        Slider slider = view.findViewById(R.id.sliderRadius);
        TextView km = view.findViewById(R.id.tvKm);
        slider.addOnChangeListener((s, value, fromUser) -> km.setText(((int) value) + "km"));

        BottomNavHelper.wire(view, this);
    }
}
