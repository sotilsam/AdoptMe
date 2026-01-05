package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class PetDescriptionFragment extends Fragment {

    private ImageButton btnBack;
    private TextView tvTitleName;
    private ImageView ivDetailImage;

    private TextView tvDescriptionBody;
    private TextView tvHostedByValue;

    public PetDescriptionFragment() {
        super(R.layout.fragment_pet_description);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        btnBack = view.findViewById(R.id.btnBack);
        tvTitleName = view.findViewById(R.id.tvTitleName);
        ivDetailImage = view.findViewById(R.id.ivDetailImage);
        tvDescriptionBody = view.findViewById(R.id.tvDescriptionBody);
        tvHostedByValue = view.findViewById(R.id.tvHostedByValue);

        // Back
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Read args
        String petId = null;
        String name = null;
        String imageUrl = null;
        String description = null;
        String shelter = null;

        if (getArguments() != null) {
            petId = getArguments().getString("petId");
            name = getArguments().getString("petName");
            imageUrl = getArguments().getString("petUrl");
            description = getArguments().getString("petDescription");
            shelter = getArguments().getString("petShelter");
        }

        // Preferred: load by id (always up to date)
        if (petId != null && !petId.trim().isEmpty()) {
            loadPet(petId);
            return;
        }

        // Fallback: show what we received in the bundle
        if (name != null) tvTitleName.setText(name);
        if (description != null) tvDescriptionBody.setText(description);
        if (shelter != null) tvHostedByValue.setText(shelter);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivDetailImage);
        } else {
            Toast.makeText(getContext(), "Missing pet data", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPet(String petId) {
        FirebaseFirestore.getInstance()
                .collection("pets")
                .document(petId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(getContext(), "Pet not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Pet pet = doc.toObject(Pet.class);
                    if (pet == null) return;

                    // Map Firestore fields
                    String name = pet.getName() != null ? pet.getName() : "";
                    String description = pet.getDescription() != null ? pet.getDescription() : "";
                    String hostedBy = pet.getLocation() != null ? pet.getLocation() : "Unknown shelter";
                    String imageUrl = pet.getImageUrl();

                    tvTitleName.setText(name);
                    tvDescriptionBody.setText(description);
                    tvHostedByValue.setText(hostedBy);

                    Glide.with(this)
                            .load(imageUrl)
                            .centerCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(ivDetailImage);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
