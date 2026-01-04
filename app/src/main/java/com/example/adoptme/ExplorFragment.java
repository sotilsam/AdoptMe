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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExplorFragment extends Fragment {

    private ImageView ivPetImage;
    private TextView tvPetName, tvPetBreed, tvPetInfo;
    private List<Pet> petList = new ArrayList<>();
    private int currentPetIndex = 0;

    public ExplorFragment() {
        super(R.layout.fragment_explor);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI from your Figma-based layout
        ivPetImage = view.findViewById(R.id.ivPetImage);
        tvPetName = view.findViewById(R.id.tvPetName);
        tvPetBreed = view.findViewById(R.id.tvPetBreed);
        tvPetInfo = view.findViewById(R.id.tvPetInfo);

        ImageButton btnReject = view.findViewById(R.id.btnRejectCircle);
        ImageButton btnInfo = view.findViewById(R.id.btnInfoCircle);
        ImageButton btnLike = view.findViewById(R.id.btnLikeCircle);

        // Fetch real data from Firebase
        fetchPetsFromFirebase();

        // X Button: Show next pet
        btnReject.setOnClickListener(v -> showNextPet());

        // Heart Button: Like pet (Add to favorites logic goes here)
        btnLike.setOnClickListener(v -> {
            if (!petList.isEmpty()) {
                Toast.makeText(getContext(), "You liked " + petList.get(currentPetIndex).getName(), Toast.LENGTH_SHORT).show();
                showNextPet();
            }
        });

        // i Button: Show details (You can navigate to a details fragment here)
        btnInfo.setOnClickListener(v -> Toast.makeText(getContext(), "Showing details...", Toast.LENGTH_SHORT).show());

        BottomNavHelper.wire(view, this);
    }

    private void fetchPetsFromFirebase() {
        FirebaseFirestore.getInstance().collection("pets")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Pet pet = doc.toObject(Pet.class);
                        petList.add(pet);
                    }
                    displayPet(currentPetIndex);
                });
    }

    private void displayPet(int index) {
        if (index < petList.size()) {
            Pet pet = petList.get(index);
            tvPetName.setText(pet.getName());
            tvPetBreed.setText(pet.getBreed());
            tvPetInfo.setText(pet.getAgeCategory() + ", " + pet.getLocation());

            Glide.with(this)
                    .load(pet.getImageUrl())
                    .centerCrop()
                    .into(ivPetImage);
        } else {
            Toast.makeText(getContext(), "No more pets nearby!", Toast.LENGTH_LONG).show();
        }
    }

    private void showNextPet() {
        currentPetIndex++;
        displayPet(currentPetIndex);
    }
}