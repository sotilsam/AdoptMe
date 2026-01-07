package com.example.adoptme;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExploreFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

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
    private int locationRadius = 30; // Default radius in km

    private GestureDetector gestureDetector;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;

    public ExploreFragment() {
        super(R.layout.fragment_explore);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

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

        // Request location permission and load pets
        requestLocationPermissionAndLoad();
    }

    private void setupSwipeGestures() {
        if (cardContainer == null) return;

        final float[] dX = {0};
        final float[] dY = {0};
        final float[] startX = {0};
        final float[] startY = {0};
        final boolean[] isDragging = {false};

        cardContainer.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Record starting position
                    dX[0] = v.getX() - event.getRawX();
                    dY[0] = v.getY() - event.getRawY();
                    startX[0] = event.getRawX();
                    startY[0] = event.getRawY();
                    isDragging[0] = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() + dX[0];
                    float deltaX = event.getRawX() - startX[0];
                    float deltaY = event.getRawY() - startY[0];

                    // Check if user is actually dragging (moved more than threshold)
                    if (!isDragging[0] && (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10)) {
                        isDragging[0] = true;
                    }

                    if (isDragging[0]) {
                        // Move card horizontally with finger
                        v.setX(newX);

                        // Add rotation effect based on swipe distance
                        float rotation = deltaX / 20f;
                        v.setRotation(rotation);

                        // Add slight scale effect
                        float scale = 1.0f - (Math.abs(deltaX) / 3000f);
                        v.setScaleX(Math.max(0.9f, scale));
                        v.setScaleY(Math.max(0.9f, scale));

                        // Change alpha based on swipe distance
                        float alpha = 1.0f - (Math.abs(deltaX) / 800f);
                        v.setAlpha(Math.max(0.5f, alpha));
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    float finalDeltaX = event.getRawX() - startX[0];
                    float threshold = v.getWidth() / 3f;

                    if (Math.abs(finalDeltaX) > threshold) {
                        // Swipe completed - animate off screen
                        if (finalDeltaX > 0) {
                            // Swipe right - Like
                            animateCardOffScreen(v, true);
                            Pet pet = getCurrentPet();
                            if (pet != null) handleLike(pet);
                        } else {
                            // Swipe left - Pass
                            animateCardOffScreen(v, false);
                            handlePass();
                        }
                    } else {
                        // Swipe not completed - animate back to center
                        v.animate()
                                .x(0)
                                .y(0)
                                .rotation(0)
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .alpha(1.0f)
                                .setDuration(200)
                                .start();
                    }
                    return true;
            }
            return false;
        });
    }

    private void animateCardOffScreen(View card, boolean swipeRight) {
        float targetX = swipeRight ? card.getWidth() * 2 : -card.getWidth() * 2;
        float rotation = swipeRight ? 30 : -30;

        card.animate()
                .x(targetX)
                .rotation(rotation)
                .alpha(0)
                .setDuration(300)
                .start();
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
                            Object radiusObj = preferences.get("locationRadius");
                            if (radiusObj instanceof Long) {
                                locationRadius = ((Long) radiusObj).intValue();
                            }
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

    private void requestLocationPermissionAndLoad() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocationAndLoad();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocationAndLoad();
            } else {
                // Permission denied, load pets without location filtering
                Toast.makeText(getContext(), "Location permission denied. Showing all pets.", Toast.LENGTH_SHORT).show();
                loadUserPreferencesAndFetchPets();
            }
        }
    }

    private void getUserLocationAndLoad() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadUserPreferencesAndFetchPets();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    userLocation = location;
                    loadUserPreferencesAndFetchPets();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                    loadUserPreferencesAndFetchPets();
                });
    }

    private double calculateDistance(GeoPoint petLocation) {
        if (userLocation == null || petLocation == null) {
            return 0;
        }

        float[] results = new float[1];
        Location.distanceBetween(
            userLocation.getLatitude(), userLocation.getLongitude(),
            petLocation.getLatitude(), petLocation.getLongitude(),
            results
        );

        return results[0] / 1000; // Convert meters to kilometers
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

                            // Filter by location radius (only if user location is available)
                            if (userLocation != null && pet.getLocation() != null) {
                                double distance = calculateDistance(pet.getLocation());
                                if (distance > locationRadius) {
                                    continue;
                                }
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
                    // Delay to let animation finish
                    if (cardContainer != null) {
                        cardContainer.postDelayed(this::showNextPet, 300);
                    }
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
                    // Delay to let animation finish
                    if (cardContainer != null) {
                        cardContainer.postDelayed(this::showNextPet, 300);
                    }
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

        // Reset card position and animate entrance
        if (cardContainer != null) {
            // Reset transformations
            cardContainer.setX(0);
            cardContainer.setY(0);
            cardContainer.setRotation(0);
            cardContainer.setScaleX(1.0f);
            cardContainer.setScaleY(1.0f);
            cardContainer.setAlpha(1.0f);

            // Animate new card entrance
            android.view.animation.Animation enterAnimation = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.card_enter);
            cardContainer.startAnimation(enterAnimation);
        }
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
