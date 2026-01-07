package com.example.adoptme;

import android.app.Dialog;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExploreFragment extends Fragment {

    private ImageView ivPetImage;
    private TextView tvPetName, tvPetBreed, tvPetInfo;

    private ImageButton btnReject, btnInfo, btnLike;
    private FrameLayout cardContainer;

    private final List<Pet> petList = new ArrayList<>();
    private int currentPetIndex = 0;

    private List<String> userPassedPets = new ArrayList<>();
    private List<String> userFavoritePets = new ArrayList<>();
    private List<String> preferredTypes = new ArrayList<>();
    private List<String> preferredSizes = new ArrayList<>();

    private GestureDetector gestureDetector;

    public ExploreFragment() {
        super(R.layout.fragment_explore);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Card UI (inside include)
        cardContainer = view.findViewById(R.id.exploreCardContainer);
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

        btnReject.setOnClickListener(v -> handlePass());

        btnInfo.setOnClickListener(v -> {
            Pet pet = getCurrentPet();
            if (pet == null) return;
            showPetDetailsDialog(pet);
        });

        btnLike.setOnClickListener(v -> {
            Pet pet = getCurrentPet();
            if (pet == null) return;
            handleLike(pet);
        });

        // Setup swipe gestures
        setupSwipeGestures();

        loadUserPreferencesAndFetchPets();
    }

    private void setupSwipeGestures() {
        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Swipe right - Like
                            Pet pet = getCurrentPet();
                            if (pet != null) handleLike(pet);
                        } else {
                            // Swipe left - Pass
                            handlePass();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        if (cardContainer != null) {
            cardContainer.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return true;
            });
        }
    }

    private void loadUserPreferencesAndFetchPets() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Get passed and favorite pets
                        userPassedPets = (List<String>) doc.get("passed");
                        if (userPassedPets == null) userPassedPets = new ArrayList<>();

                        userFavoritePets = (List<String>) doc.get("favorites");
                        if (userFavoritePets == null) userFavoritePets = new ArrayList<>();

                        // Get preferences
                        Map<String, Object> preferences = (Map<String, Object>) doc.get("preferences");
                        if (preferences != null) {
                            preferredTypes = (List<String>) preferences.get("type");
                            preferredSizes = (List<String>) preferences.get("size");
                        }
                        if (preferredTypes == null) preferredTypes = new ArrayList<>();
                        if (preferredSizes == null) preferredSizes = new ArrayList<>();
                    }
                    fetchPetsFromFirebase();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    fetchPetsFromFirebase();
                });
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
                            pet.setId(doc.getId());

                            // Filter: skip if already passed or favorited
                            if (userPassedPets.contains(pet.getId()) || userFavoritePets.contains(pet.getId())) {
                                continue;
                            }

                            // Filter by type preference
                            if (!preferredTypes.isEmpty() && pet.getType() != null && !preferredTypes.contains(pet.getType())) {
                                continue;
                            }

                            // Filter by size preference
                            if (!preferredSizes.isEmpty() && pet.getSize() != null && !preferredSizes.contains(pet.getSize())) {
                                continue;
                            }

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
            return null;
        }
        if (currentPetIndex < 0 || currentPetIndex >= petList.size()) {
            return null;
        }
        return petList.get(currentPetIndex);
    }

    private void displayCurrentPet() {
        Pet pet = getCurrentPet();
        if (pet == null) {
            tvPetName.setText("No pets available");
            tvPetBreed.setText("");
            tvPetInfo.setText("");
            ivPetImage.setImageResource(android.R.drawable.ic_menu_gallery);
            return;
        }

        tvPetName.setText(pet.getName() != null ? pet.getName() : "");
        tvPetBreed.setText(pet.getBreed() != null ? pet.getBreed() : "");

        String info = "";
        if (pet.getAgeCategory() != null) info += pet.getAgeCategory();
        if (pet.getLocation() != null) info += (info.isEmpty() ? "" : ", ") + pet.getLocationString();
        tvPetInfo.setText(info);

        Glide.with(this)
                .load(pet.getImageUrl())
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivPetImage);
    }

    private void handlePass() {
        Pet pet = getCurrentPet();
        if (pet == null) return;

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        // Add to passed array
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("passed", FieldValue.arrayUnion(pet.getId()))
                .addOnSuccessListener(aVoid -> {
                    userPassedPets.add(pet.getId());
                    showNextPet();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to pass pet: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void handleLike(Pet pet) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        // Add to favorites array
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("favorites", FieldValue.arrayUnion(pet.getId()))
                .addOnSuccessListener(aVoid -> {
                    userFavoritePets.add(pet.getId());
                    showContactDialog(pet);
                    showNextPet();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to add to favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showNextPet() {
        if (petList.isEmpty()) {
            displayCurrentPet();
            return;
        }

        currentPetIndex++;

        if (currentPetIndex >= petList.size()) {
            Toast.makeText(getContext(), "No more pets! Check your preferences.", Toast.LENGTH_SHORT).show();
            currentPetIndex = petList.size() - 1;
            return;
        }

        displayCurrentPet();
    }

    private void showPetDetailsDialog(Pet pet) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_pet_details);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView ivPet = dialog.findViewById(R.id.ivDialogPetImage);
        TextView tvName = dialog.findViewById(R.id.tvDialogPetName);
        TextView tvBreed = dialog.findViewById(R.id.tvDialogBreed);
        TextView tvAge = dialog.findViewById(R.id.tvDialogAge);
        TextView tvGender = dialog.findViewById(R.id.tvDialogGender);
        TextView tvSize = dialog.findViewById(R.id.tvDialogSize);
        Button btnLocation = dialog.findViewById(R.id.btnDialogLocation);
        TextView tvDescription = dialog.findViewById(R.id.tvDialogDescription);
        Button btnClose = dialog.findViewById(R.id.btnDialogClose);

        Glide.with(this)
                .load(pet.getImageUrl())
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivPet);

        tvName.setText(pet.getName() != null ? pet.getName() : "Unknown");
        tvBreed.setText("Breed: " + (pet.getBreed() != null ? pet.getBreed() : "Unknown"));
        tvAge.setText("Age: " + (pet.getAgeCategory() != null ? pet.getAgeCategory() : "Unknown"));
        tvGender.setText("Gender: " + (pet.getGender() != null ? pet.getGender() : "Unknown"));
        tvSize.setText("Size: " + (pet.getSize() != null ? pet.getSize() : "Unknown"));
        tvDescription.setText(pet.getDescription() != null ? pet.getDescription() : "No description available");

        // Handle location button
        if (pet.getLocation() != null) {
            btnLocation.setVisibility(View.VISIBLE);
            btnLocation.setOnClickListener(v -> openGoogleMaps(pet.getLocation()));
        } else {
            btnLocation.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openGoogleMaps(com.google.firebase.firestore.GeoPoint location) {
        if (location == null) return;

        String uri = String.format("geo:%f,%f?q=%f,%f",
            location.getLatitude(), location.getLongitude(),
            location.getLatitude(), location.getLongitude());

        android.content.Intent mapIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback to browser if Google Maps not installed
            String browserUri = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f",
                location.getLatitude(), location.getLongitude());
            android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(browserUri));
            startActivity(browserIntent);
        }
    }

    private void showContactDialog(Pet pet) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_contact_shelter);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvMessage = dialog.findViewById(R.id.tvContactMessage);
        Button btnEmail = dialog.findViewById(R.id.btnContactEmail);
        Button btnPhone = dialog.findViewById(R.id.btnContactPhone);
        Button btnClose = dialog.findViewById(R.id.btnContactClose);

        tvMessage.setText("Would you like to contact the shelter about " + (pet.getName() != null ? pet.getName() : "this pet") + "?");

        if (pet.getContactEmail() != null && !pet.getContactEmail().isEmpty()) {
            btnEmail.setText("Email: " + pet.getContactEmail());
            btnEmail.setVisibility(View.VISIBLE);
            btnEmail.setOnClickListener(v -> {
                android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                emailIntent.setData(android.net.Uri.parse("mailto:" + pet.getContactEmail()));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Inquiry about " + pet.getName());
                startActivity(android.content.Intent.createChooser(emailIntent, "Send email"));
            });
        } else {
            btnEmail.setVisibility(View.GONE);
        }

        if (pet.getContactPhone() != null && !pet.getContactPhone().isEmpty()) {
            btnPhone.setText("Call: " + pet.getContactPhone());
            btnPhone.setVisibility(View.VISIBLE);
            btnPhone.setOnClickListener(v -> {
                android.content.Intent dialIntent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                dialIntent.setData(android.net.Uri.parse("tel:" + pet.getContactPhone()));
                startActivity(dialIntent);
            });
        } else {
            btnPhone.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
