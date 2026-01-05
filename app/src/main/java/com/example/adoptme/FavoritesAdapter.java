package com.example.adoptme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private final List<Pet> favoritePets;

    public FavoritesAdapter(List<Pet> favoritePets) {
        this.favoritePets = favoritePets;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Pet pet = favoritePets.get(position);

        // Load image
        Glide.with(holder.itemView.getContext())
                .load(pet.getImageUrl())
                .centerCrop()
                .into(holder.ivPet);


        // Short click: open description
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();

            // Preferred: use petId and load the rest from Firestore
            bundle.putString("petId", pet.getId());

            // Fallback: pass these too (so the screen can still show data if id is missing)
            bundle.putString("petName", pet.getName());
            bundle.putString("petUrl", pet.getImageUrl());
            bundle.putString("petDescription", pet.getDescription());
            bundle.putString("petShelter", pet.getLocation());

            Navigation.findNavController(v).navigate(R.id.action_favorites_to_description, bundle);
        });

        // Long click: remove from favorites
        holder.itemView.setOnLongClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) return true;

            // Use the document id if you have it, otherwise fallback to name
            String favDocId = (pet.getId() != null && !pet.getId().trim().isEmpty())
                    ? pet.getId()
                    : pet.getName();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(favDocId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        int adapterPos = holder.getAdapterPosition();
                        if (adapterPos == RecyclerView.NO_POSITION) return;

                        favoritePets.remove(adapterPos);
                        notifyItemRemoved(adapterPos);
                        notifyItemRangeChanged(adapterPos, favoritePets.size());

                        android.widget.Toast.makeText(
                                v.getContext(),
                                "Removed from favorites",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();
                    })
                    .addOnFailureListener(e -> android.widget.Toast.makeText(
                            v.getContext(),
                            "Failed: " + e.getMessage(),
                            android.widget.Toast.LENGTH_LONG
                    ).show());

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return favoritePets.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPet;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPet = itemView.findViewById(R.id.ivFavPet);
        }
    }
}
