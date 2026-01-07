package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileMenuFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvName, tvEmail, tvPhone;

    public ProfileMenuFragment() {
        super(R.layout.fragment_profile_menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);

        loadUserData();

        // Navigation Buttons
        view.findViewById(R.id.btnPreferences).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profileMenu_to_preferences));

        // Logout Logic
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
        });
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();

        // Set email immediately from FirebaseAuth
        if (userEmail != null) {
            tvEmail.setText(userEmail);
        }

        // Fetch additional data from Firestore
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String phone = documentSnapshot.getString("phone");

                        tvName.setText(fullName != null ? fullName : "N/A");
                        tvPhone.setText(phone != null ? phone : "N/A");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }
}