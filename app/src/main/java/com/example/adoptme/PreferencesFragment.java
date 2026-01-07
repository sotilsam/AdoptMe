package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferencesFragment extends Fragment {

    private TextView tvRadiusValue;
    private SeekBar sbRadius;
    private CheckBox cbDog, cbCat, cbSmall, cbMedium, cbLarge;
    private Button btnSave;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public PreferencesFragment() {
        super(R.layout.fragment_preferences);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvRadiusValue = view.findViewById(R.id.tvRadiusValue);
        sbRadius = view.findViewById(R.id.sbRadius);
        cbDog = view.findViewById(R.id.cbDog);
        cbCat = view.findViewById(R.id.cbCat);
        cbSmall = view.findViewById(R.id.cbSmall);
        cbMedium = view.findViewById(R.id.cbMedium);
        cbLarge = view.findViewById(R.id.cbLarge);
        btnSave = view.findViewById(R.id.btnSavePreferences);

        // Slider logic for radius
        sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRadiusValue.setText(progress + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Load current preferences
        loadPreferences();

        // Save button
        btnSave.setOnClickListener(v -> savePreferences());
    }

    private void loadPreferences() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Object> preferences = (Map<String, Object>) doc.get("preferences");
                        if (preferences != null) {
                            // Load radius
                            Object radiusObj = preferences.get("locationRadius");
                            if (radiusObj instanceof Long) {
                                int radius = ((Long) radiusObj).intValue();
                                sbRadius.setProgress(radius);
                                tvRadiusValue.setText(radius + "km");
                            }

                            // Load type preferences
                            List<String> types = (List<String>) preferences.get("type");
                            if (types != null) {
                                cbDog.setChecked(types.contains("Dog"));
                                cbCat.setChecked(types.contains("Cat"));
                            }

                            // Load size preferences
                            List<String> sizes = (List<String>) preferences.get("size");
                            if (sizes != null) {
                                cbSmall.setChecked(sizes.contains("Small"));
                                cbMedium.setChecked(sizes.contains("Medium"));
                                cbLarge.setChecked(sizes.contains("Large"));
                            }
                        }
                    }
                });
    }

    private void savePreferences() {
        String userId = mAuth.getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get radius
        int radius = sbRadius.getProgress();

        // Get selected types
        List<String> selectedTypes = new ArrayList<>();
        if (cbDog.isChecked()) selectedTypes.add("Dog");
        if (cbCat.isChecked()) selectedTypes.add("Cat");

        // Get selected sizes
        List<String> selectedSizes = new ArrayList<>();
        if (cbSmall.isChecked()) selectedSizes.add("Small");
        if (cbMedium.isChecked()) selectedSizes.add("Medium");
        if (cbLarge.isChecked()) selectedSizes.add("Large");

        // Validation
        if (selectedTypes.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one pet type", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSizes.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one size", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create preferences map
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("locationRadius", radius);
        preferences.put("type", selectedTypes);
        preferences.put("size", selectedSizes);

        // Save to Firestore
        db.collection("users").document(userId)
                .update("preferences", preferences)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Preferences saved!", Toast.LENGTH_SHORT).show();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
