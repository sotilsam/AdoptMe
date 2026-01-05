package com.example.adoptme;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private ImageView ivPetImage;
    private TextView tvPetName, tvPetBreed, tvPetInfo;

    private ImageButton btnReject, btnInfo, btnLike;

    private final List<Pet> petList = new ArrayList<>();
    private int currentPetIndex = 0;

    public ExploreFragment() {
        super(R.layout.fragment_explore);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Card UI (inside include)
        FrameLayout cardContainer = view.findViewById(R.id.exploreCardContainer);
        if (cardContainer != null) {
            ivPetImage = cardContainer.findViewById(R.id.ivPetImage);
            tvPetName = cardContainer.findViewById(R.id.tvPetName);
            tvPetBreed = cardContainer.findViewById(R.id.tvPetBreed);
            tvPetInfo = cardContainer.findViewById(R.id.tvPetInfo);
        }

        // Buttons
        btnReject = view.findViewById(R.id.btnRejectCircle);
        btnInfo = view.findViewById(R.id.btnInfoCircle);
        btnLike = view.findViewById(R.id.btnLikeCircle);

        btnReject.setOnClickListener(v -> showNextPet());

        btnInfo.setOnClickListener(v -> {
            Pet pet = getCurrentPet();
            if (pet == null) return;

            Bundle b = new Bundle();
            b.putString("petId", pet.getId()); // main way
            b.putString("petName", pet.getName()); // fallback
            b.putString("petUrl", pet.getImageUrl());
            b.putString("petDescription", pet.getDescription());
            b.putString("petShelter", pet.getLocation());

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_explore_to_description, b);
        });

        btnLike.setOnClickListener(v -> {
            Pet pet = getCurrentPet();
            if (pet == null) return;
            showMessageDialog(pet);
        });

        fetchPetsFromFirebase();
    }

    private void fetchPetsFromFirebase() {
        FirebaseFirestore.getInstance()
                .collection("pets")
                .get()
                .addOnSuccessListener(query -> {
                    petList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Pet pet = doc.toObject(Pet.class);
                        if (pet != null) {
                            pet.setId(doc.getId()); // IMPORTANT: keep doc id
                            petList.add(pet);
                        }
                    }
                    currentPetIndex = 0;
                    displayCurrentPet();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load pets: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private Pet getCurrentPet() {
        if (petList.isEmpty()) {
            Toast.makeText(getContext(), "No pets available", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (currentPetIndex < 0 || currentPetIndex >= petList.size()) {
            Toast.makeText(getContext(), "No more pets!", Toast.LENGTH_SHORT).show();
            return null;
        }
        return petList.get(currentPetIndex);
    }

    private void displayCurrentPet() {
        Pet pet = getCurrentPet();
        if (pet == null) return;

        tvPetName.setText(pet.getName() != null ? pet.getName() : "");
        tvPetBreed.setText(pet.getBreed() != null ? pet.getBreed() : "");

        String info = "";
        if (pet.getAgeCategory() != null) info += pet.getAgeCategory();
        if (pet.getLocation() != null) info += (info.isEmpty() ? "" : ", ") + pet.getLocation();
        tvPetInfo.setText(info);

        Glide.with(this)
                .load(pet.getImageUrl())
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivPetImage);
    }

    private void showNextPet() {
        if (petList.isEmpty()) {
            Toast.makeText(getContext(), "No pets available", Toast.LENGTH_SHORT).show();
            return;
        }

        currentPetIndex++;

        if (currentPetIndex >= petList.size()) {
            Toast.makeText(getContext(), "No more pets!", Toast.LENGTH_SHORT).show();
            currentPetIndex = petList.size() - 1;
            return;
        }

        displayCurrentPet();
    }

    private void showMessageDialog(Pet pet) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.layout_confirm_dialog);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            addToFavorites(pet);

            Bundle args = new Bundle();
            args.putString("petId", pet.getId());
            args.putString("petName", pet.getName());
            args.putString("petImage", pet.getImageUrl());

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_explore_to_matchSuccess, args);

            showNextPet();
        });

        btnNo.setOnClickListener(v -> {
            dialog.dismiss();
            showNextPet();
        });

        dialog.show();
    }

    private void addToFavorites(Pet pet) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        String favDocId = (pet.getId() != null && !pet.getId().trim().isEmpty())
                ? pet.getId()
                : (pet.getName() != null ? pet.getName() : String.valueOf(System.currentTimeMillis()));

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("favorites")
                .document(favDocId)
                .set(pet);
    }
}
