package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileMenuFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvName;

    public ProfileMenuFragment() {
        super(R.layout.fragment_profile_menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tvName = view.findViewById(R.id.tvName);

        // Fetch real user name from Firestore to replace "Tomer"
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("fullName");
                    if (name != null) tvName.setText(name);
                }
            });
        }

        // Navigation Buttons
        view.findViewById(R.id.btnPersonalDetails).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profileMenu_to_personalDetails));

        view.findViewById(R.id.btnPreferences).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profileMenu_to_preferences));

        // Logout Logic
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
        });

    }
}