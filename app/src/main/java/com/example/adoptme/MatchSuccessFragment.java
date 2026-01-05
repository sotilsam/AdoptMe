package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MatchSuccessFragment extends Fragment {

    private ImageView ivMatchedPetCircle, ivSmallPetThumb;
    private TextView tvMatchMessage;
    private String currentUserName = "User";

    public MatchSuccessFragment() {
        super(R.layout.fragment_match_success);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components from your layout_match_success.xml
        ivMatchedPetCircle = view.findViewById(R.id.ivMatchedPetCircle);
        ivSmallPetThumb = view.findViewById(R.id.ivSmallPetThumb);
        tvMatchMessage = view.findViewById(R.id.tvMatchMessage);
        Button btnMessageShelter = view.findViewById(R.id.btnMessageShelter);

        // 1. Fetch the actual user's name from Firestore for the message
        fetchUserName();

        // 2. Get passed arguments (Pet Name and Image) from ExploreFragment
        if (getArguments() != null) {
            String petName = getArguments().getString("petName");
            String petImage = getArguments().getString("petImage");

            // Update the green bubble message dynamically
            updateMessage(petName);

            // Load pet images into circular views using Glide
            if (petImage != null) {
                Glide.with(this).load(petImage).circleCrop().into(ivMatchedPetCircle);
                Glide.with(this).load(petImage).circleCrop().into(ivSmallPetThumb);
            }
        }

        // 3. Handle 'Message Shelter' button click
        btnMessageShelter.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.chatListFragment);
        });
    }

    private void fetchUserName() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            currentUserName = doc.getString("fullName");
                            // Refresh message if pet data was already loaded
                            if (getArguments() != null) {
                                updateMessage(getArguments().getString("petName"));
                            }
                        }
                    });
        }
    }

    private void updateMessage(String petName) {
        String fullMessage = "Hello " + currentUserName + "\n" +
                "You matched with " + petName + "!\n\n" +
                "Want to take the next step? Tap below to message the shelter, ask questions, and set up a meet up and greet!";
        tvMatchMessage.setText(fullMessage);
    }
}