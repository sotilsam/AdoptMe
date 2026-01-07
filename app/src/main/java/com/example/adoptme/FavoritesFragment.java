package com.example.adoptme;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private FavoritesAdapter adapter;
    private final List<Pet> favoritePets = new ArrayList<>();

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorites = view.findViewById(R.id.rvFavorites);

        // 2 items per row
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvFavorites.setLayoutManager(gridLayoutManager);

        // Improves performance when layout size doesn’t change
        rvFavorites.setHasFixedSize(true);

        // Add spacing between items (only add once)
        if (rvFavorites.getItemDecorationCount() == 0) {
            int spacingPx = dpToPx(24);
            rvFavorites.addItemDecoration(new GridSpacingItemDecoration(2, spacingPx));
        }

        adapter = new FavoritesAdapter(favoritePets);
        rvFavorites.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // First, get the user's favorites array
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) return;

                    List<String> favoriteIds = (List<String>) userDoc.get("favorites");
                    if (favoriteIds == null || favoriteIds.isEmpty()) {
                        favoritePets.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    // Now fetch each pet by ID
                    favoritePets.clear();
                    for (String petId : favoriteIds) {
                        FirebaseFirestore.getInstance()
                                .collection("pets")
                                .document(petId)
                                .get()
                                .addOnSuccessListener(petDoc -> {
                                    if (petDoc.exists()) {
                                        Pet pet = petDoc.toObject(Pet.class);
                                        if (pet != null) {
                                            pet.setId(petDoc.getId());
                                            favoritePets.add(pet);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load favorites: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // --- Helpers ---

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;

        GridSpacingItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position == RecyclerView.NO_POSITION) return;

            int column = position % spanCount;

            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;
            outRect.bottom = spacing;

            // Top spacing only for first row
            if (position < spanCount) {
                outRect.top = spacing;
            }
        }
    }
}
