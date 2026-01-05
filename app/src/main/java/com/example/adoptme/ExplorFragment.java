package com.example.adoptme;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation; // וודאי שיש את זה

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

        // חיבור לרכיבים במסך
        ivPetImage = view.findViewById(R.id.ivPetImage);
        tvPetName = view.findViewById(R.id.tvPetName);
        tvPetBreed = view.findViewById(R.id.tvPetBreed);
        tvPetInfo = view.findViewById(R.id.tvPetInfo);

        ImageButton btnReject = view.findViewById(R.id.btnRejectCircle);
        ImageButton btnInfo = view.findViewById(R.id.btnInfoCircle);
        ImageButton btnLike = view.findViewById(R.id.btnLikeCircle);

        // טעינת הנתונים מ-Firebase
        fetchPetsFromFirebase();

        // כפתור איקס - עבור לחיה הבאה
        btnReject.setOnClickListener(v -> showNextPet());

        // כפתור לב - הצגת הדיאלוג
        btnLike.setOnClickListener(v -> {
            if (!petList.isEmpty() && currentPetIndex < petList.size()) {
                Pet currentPet = petList.get(currentPetIndex);
                showMessageDialog(currentPet); // קריאה לדיאלוג החדש
            }
        });

        // כפתור מידע (כרגע רק הודעה)
        btnInfo.setOnClickListener(v -> Toast.makeText(getContext(), "Showing details...", Toast.LENGTH_SHORT).show());
    }

    // --- פונקציה לשליפת נתונים ---
    private void fetchPetsFromFirebase() {
        FirebaseFirestore.getInstance().collection("pets")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    petList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Pet pet = doc.toObject(Pet.class);
                        petList.add(pet);
                    }
                    displayPet(currentPetIndex);
                });
    }

    // --- פונקציה להצגת חיה על המסך ---
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
            Toast.makeText(getContext(), "No more pets nearby!", Toast.LENGTH_SHORT).show();
            // אופציונלי: כאן אפשר להסתיר את הכרטיס או להציג הודעת סיום
        }
    }

    // --- פונקציה להצגת הדיאלוג (מה שחסר לך) ---
    private void showMessageDialog(Pet pet) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_message_shelter);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        // --- לחיצה על YES: שומרים והולכים לצ'אט ---
        // --- לחיצה על YES ---
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            addToFavorites(pet); // שמירה במועדפים

            // הכנת המידע למעבר (שם ותמונה)
            Bundle args = new Bundle();
            args.putString("petName", pet.getName());
            args.putString("petImage", pet.getImageUrl());

            // מעבר למסך ה-Match הצהוב עם המידע
            androidx.navigation.fragment.NavHostFragment.findNavController(this)
                    .navigate(R.id.matchFragment, args);
        });

        // --- לחיצה על NO: שומרים ועוברים לחיה הבאה ---
        btnNo.setOnClickListener(v -> {
            dialog.dismiss();
            addToFavorites(pet); // שמירה במועדפים (כי עשינו לייק)
            showNextPet();       // טוען את הכלב הבא
        });

        dialog.show();
    }

    // --- פונקציה לשמירה במועדפים (הייתה חסרה לך) ---
    private void addToFavorites(Pet pet) {
        FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(pet.getName())
                .set(pet)
                .addOnSuccessListener(aVoid -> {
                    // נשמר בהצלחה (לא חייב להקפיץ הודעה אם זה מפריע לזרימה)
                });
    }

    // --- פונקציה למעבר לחיה הבאה (הייתה חסרה לך) ---
    private void showNextPet() {
        currentPetIndex++;
        displayPet(currentPetIndex);
    }
}