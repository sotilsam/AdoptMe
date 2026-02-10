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
    private ImageView pbPetImageLoading; // Changed from ProgressBar to ImageView
    private androidx.constraintlayout.widget.ConstraintLayout cardContent;
    private View darkOverlay;

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

    // Store the card's initial centered position
    private float cardInitialX = 0f;
    private float cardInitialY = 0f;
    private boolean cardPositionCaptured = false;

    public ExploreFragment() {
        super(R.layout.fragment_explore);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.util.Log.d("ExploreFragment", "=== ExploreFragment onViewCreated ===");

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Card UI (inside include)
        cardContainer = view.findViewById(R.id.exploreCardContainer);
        if (cardContainer != null) {
            ivPetImage = cardContainer.findViewById(R.id.ivPetImage);
            tvPetName = cardContainer.findViewById(R.id.tvPetName);
            tvPetBreed = cardContainer.findViewById(R.id.tvPetBreed);
            tvPetInfo = cardContainer.findViewById(R.id.tvPetInfo);
            pbPetImageLoading = cardContainer.findViewById(R.id.pbPetImageLoading);
            cardContent = cardContainer.findViewById(R.id.cardContent);
            darkOverlay = cardContainer.findViewById(R.id.darkOverlay);
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

        // Capture the card's initial centered position once the view is laid out
        cardContainer.post(() -> {
            if (!cardPositionCaptured) {
                cardInitialX = cardContainer.getX();
                cardInitialY = cardContainer.getY();
                cardPositionCaptured = true;
                android.util.Log.d("ExploreFragment", "Captured card initial position: x=" + cardInitialX + ", y=" + cardInitialY);
            }
        });

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
                        // Swipe not completed - animate back to initial position
                        android.util.Log.d("ExploreFragment", "Resetting card to initial position: x=" + cardInitialX + ", y=" + cardInitialY + " (current: x=" + v.getX() + ", y=" + v.getY() + ")");
                        v.animate()
                                .x(cardInitialX)
                                .y(cardInitialY)
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
        android.util.Log.d("ExploreFragment", "loadUserPreferencesAndFetchPets - userId: " + userId);

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
                    if (location == null) {
                        // Use Tel Aviv as default location if GPS not available
                        userLocation = new Location("default");
                        userLocation.setLatitude(32.0853);  // Tel Aviv latitude
                        userLocation.setLongitude(34.7818); // Tel Aviv longitude
                        android.util.Log.d("ExploreFragment", "GPS not available, using Tel Aviv");
                    } else {
                        // Check if location is in Israel (rough bounding box)
                        // Israel bounds: lat 29-33.5, lon 34-36
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        boolean isInIsrael = (lat >= 29.0 && lat <= 33.5) && (lon >= 34.0 && lon <= 36.0);

                        if (isInIsrael) {
                            userLocation = location;
                            android.util.Log.d("ExploreFragment", "Using GPS location in Israel: " + lat + ", " + lon);
                        } else {
                            // Override with Tel Aviv if outside Israel
                            userLocation = new Location("default");
                            userLocation.setLatitude(32.0853);  // Tel Aviv latitude
                            userLocation.setLongitude(34.7818); // Tel Aviv longitude
                            android.util.Log.d("ExploreFragment", "Location outside Israel (" + lat + ", " + lon + "), using Tel Aviv");
                        }
                    }
                    loadUserPreferencesAndFetchPets();
                })
                .addOnFailureListener(e -> {
                    // Use Tel Aviv as default location on error
                    userLocation = new Location("default");
                    userLocation.setLatitude(32.0853);  // Tel Aviv latitude
                    userLocation.setLongitude(34.7818); // Tel Aviv longitude
                    android.util.Log.d("ExploreFragment", "Location error, using Tel Aviv");
                    Toast.makeText(getContext(), "Using default location: Tel Aviv", Toast.LENGTH_SHORT).show();
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
                    int totalPets = query.size();
                    int filteredCount = 0;

                    android.util.Log.d("ExploreFragment", "Total pets in database: " + totalPets);
                    android.util.Log.d("ExploreFragment", "User location: " + userLocation);
                    android.util.Log.d("ExploreFragment", "Location radius: " + locationRadius);
                    android.util.Log.d("ExploreFragment", "Preferred types: " + preferredTypes);
                    android.util.Log.d("ExploreFragment", "Preferred sizes: " + preferredSizes);

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Pet pet = doc.toObject(Pet.class);
                        if (pet != null) {
                            pet.setId(doc.getId());

                            // Filter: skip if already passed or favorited
                            if (userPassedPets.contains(pet.getId()) || userFavoritePets.contains(pet.getId())) {
                                android.util.Log.d("ExploreFragment", "Filtered (already seen): " + pet.getName());
                                filteredCount++;
                                continue;
                            }

                            // Filter by type preference
                            if (!preferredTypes.isEmpty() && pet.getType() != null && !preferredTypes.contains(pet.getType())) {
                                android.util.Log.d("ExploreFragment", "Filtered (type): " + pet.getName() + " - " + pet.getType());
                                filteredCount++;
                                continue;
                            }

                            // Filter by size preference
                            if (!preferredSizes.isEmpty() && pet.getSize() != null && !preferredSizes.contains(pet.getSize())) {
                                android.util.Log.d("ExploreFragment", "Filtered (size): " + pet.getName() + " - " + pet.getSize());
                                filteredCount++;
                                continue;
                            }

                            // Filter by location radius (only if user location is available)
                            if (userLocation != null && pet.getLocation() != null) {
                                double distance = calculateDistance(pet.getLocation());
                                if (distance > locationRadius) {
                                    android.util.Log.d("ExploreFragment", "Filtered (distance): " + pet.getName() + " - " + distance + "km");
                                    filteredCount++;
                                    continue;
                                }
                            }

                            petList.add(pet);
                            android.util.Log.d("ExploreFragment", "Added pet: " + pet.getName());
                        }
                    }

                    android.util.Log.d("ExploreFragment", "Filtered out: " + filteredCount + " pets");
                    android.util.Log.d("ExploreFragment", "Final pet list size: " + petList.size());

                    currentPetIndex = 0;
                    displayCurrentPet();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ExploreFragment", "Failed to load pets", e);
                    Toast.makeText(getContext(), "Failed to load pets: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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
            if (pbPetImageLoading != null) pbPetImageLoading.setVisibility(View.GONE);
            return;
        }

        tvPetName.setText(pet.getName() != null ? pet.getName() : "");
        tvPetBreed.setText(pet.getBreed() != null ? pet.getBreed() : "");

        String info = "";
        if (pet.getAgeCategory() != null) info += pet.getAgeCategory();
        tvPetInfo.setText(info);

        // Show loading spinner with rotation animation
        if (pbPetImageLoading != null) {
            android.util.Log.d("ExploreFragment", "Setting up spinner animation");
            android.util.Log.d("ExploreFragment", "Spinner view: " + pbPetImageLoading);
            android.util.Log.d("ExploreFragment", "Spinner visibility before: " + pbPetImageLoading.getVisibility());

            // Set background to app_bg color while loading
            if (cardContent != null) {
                cardContent.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_bg));
            }

            // Hide dark overlay while loading
            if (darkOverlay != null) {
                darkOverlay.setVisibility(View.GONE);
            }

            pbPetImageLoading.setVisibility(View.VISIBLE);
            android.util.Log.d("ExploreFragment", "Spinner visibility after: " + pbPetImageLoading.getVisibility());

            // Create and start rotation animation programmatically
            android.view.animation.RotateAnimation rotation = new android.view.animation.RotateAnimation(
                0f, 360f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotation.setDuration(250); // 250ms per rotation = 4 rotations per second (1000ms / 4 = 250ms)
            rotation.setRepeatCount(android.view.animation.Animation.INFINITE);
            rotation.setInterpolator(new android.view.animation.LinearInterpolator());
            rotation.setFillAfter(true);

            android.util.Log.d("ExploreFragment", "Starting animation with duration: " + rotation.getDuration());
            pbPetImageLoading.startAnimation(rotation);
            android.util.Log.d("ExploreFragment", "Animation started. Has animation: " + (pbPetImageLoading.getAnimation() != null));
        } else {
            android.util.Log.e("ExploreFragment", "pbPetImageLoading is NULL!");
        }

        Glide.with(this)
                .load(pet.getImageUrl())
                .centerCrop()
                .skipMemoryCache(true) // Temporarily disable cache to see spinner
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                // No placeholder - spinner is already visible behind
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        // Hide spinner on failure
                        if (pbPetImageLoading != null) {
                            android.util.Log.d("ExploreFragment", "Image load FAILED - stopping spinner");
                            pbPetImageLoading.clearAnimation();
                            pbPetImageLoading.setVisibility(View.GONE);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                   com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        // Hide spinner on success
                        if (pbPetImageLoading != null) {
                            android.util.Log.d("ExploreFragment", "Image load SUCCESS - stopping spinner");
                            pbPetImageLoading.clearAnimation();
                            pbPetImageLoading.setVisibility(View.GONE);
                            // Make background transparent now that image is loaded
                            if (cardContent != null) {
                                cardContent.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                            }
                            // Show dark overlay now that image is visible
                            if (darkOverlay != null) {
                                darkOverlay.setVisibility(View.VISIBLE);
                            }
                        }
                        return false;
                    }
                })
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

        // Reset card position FIRST before displaying new pet
        if (cardContainer != null) {
            android.util.Log.d("ExploreFragment", "Resetting card to initial centered position: x=" + cardInitialX + ", y=" + cardInitialY);

            // Reset transformations to initial centered position immediately
            cardContainer.setX(cardInitialX);
            cardContainer.setY(cardInitialY);
            cardContainer.setRotation(0);
            cardContainer.setScaleX(1.0f);
            cardContainer.setScaleY(1.0f);
            cardContainer.setAlpha(1.0f);

            // Animate new card entrance
            android.view.animation.Animation enterAnimation = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.card_enter);
            cardContainer.startAnimation(enterAnimation);
        }

        // Display new pet AFTER resetting position
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
        // Check if fragment is still attached to avoid crash
        if (!isAdded() || getContext() == null) {
            android.util.Log.w("ExploreFragment", "Fragment not attached, skipping contact dialog");
            return;
        }

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

        // Move to next pet only when dialog is dismissed
        dialog.setOnDismissListener(d -> {
            if (cardContainer != null) {
                cardContainer.postDelayed(this::showNextPet, 300);
            }
        });

        dialog.show();
    }
}
