package com.example.adoptme;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private final List<Pet> favoritePets;

    public FavoritesAdapter(List<Pet> favoritePets) {
        this.favoritePets = favoritePets;
    }

    @Override
    @NonNull
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
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

        // Click: show pet details and contact dialog
        holder.itemView.setOnClickListener(v -> showPetDetailsAndContactDialog(v, pet));

        // Long click: remove from favorites
        holder.itemView.setOnLongClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) return true;

            // Remove from favorites array
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("favorites", FieldValue.arrayRemove(pet.getId()))
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
                            "Failed to remove: " + e.getMessage(),
                            android.widget.Toast.LENGTH_LONG
                    ).show());

            return true;
        });
    }

    private void showPetDetailsAndContactDialog(View view, Pet pet) {
        Dialog dialog = new Dialog(view.getContext());
        dialog.setContentView(R.layout.dialog_favorite_pet_detail);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView ivPet = dialog.findViewById(R.id.ivFavDetailPetImage);
        TextView tvName = dialog.findViewById(R.id.tvFavDetailPetName);
        TextView tvBreed = dialog.findViewById(R.id.tvFavDetailBreed);
        TextView tvAge = dialog.findViewById(R.id.tvFavDetailAge);
        TextView tvGender = dialog.findViewById(R.id.tvFavDetailGender);
        TextView tvSize = dialog.findViewById(R.id.tvFavDetailSize);
        TextView tvLocation = dialog.findViewById(R.id.tvFavDetailLocation);
        TextView tvDescription = dialog.findViewById(R.id.tvFavDetailDescription);
        TextView tvEmail = dialog.findViewById(R.id.tvFavDetailEmail);
        TextView tvPhone = dialog.findViewById(R.id.tvFavDetailPhone);
        Button btnContact = dialog.findViewById(R.id.btnFavContact);
        Button btnClose = dialog.findViewById(R.id.btnFavDetailClose);

        Glide.with(view.getContext())
                .load(pet.getImageUrl())
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivPet);

        tvName.setText(pet.getName() != null ? pet.getName() : "Unknown");
        tvBreed.setText("Breed: " + (pet.getBreed() != null ? pet.getBreed() : "Unknown"));
        tvAge.setText("Age: " + (pet.getAgeCategory() != null ? pet.getAgeCategory() : "Unknown"));
        tvGender.setText("Gender: " + (pet.getGender() != null ? pet.getGender() : "Unknown"));
        tvSize.setText("Size: " + (pet.getSize() != null ? pet.getSize() : "Unknown"));
        tvLocation.setText("Location: " + (pet.getLocation() != null ? pet.getLocation() : "Unknown"));
        tvDescription.setText(pet.getDescription() != null ? pet.getDescription() : "No description available");

        // Show contact info if available
        boolean hasContact = false;
        if (pet.getContactEmail() != null && !pet.getContactEmail().isEmpty()) {
            tvEmail.setText("Email: " + pet.getContactEmail());
            tvEmail.setVisibility(View.VISIBLE);
            hasContact = true;
        } else {
            tvEmail.setVisibility(View.GONE);
        }

        if (pet.getContactPhone() != null && !pet.getContactPhone().isEmpty()) {
            tvPhone.setText("Phone: " + pet.getContactPhone());
            tvPhone.setVisibility(View.VISIBLE);
            hasContact = true;
        } else {
            tvPhone.setVisibility(View.GONE);
        }

        // Show or hide contact button based on availability
        if (hasContact) {
            btnContact.setVisibility(View.VISIBLE);
            btnContact.setOnClickListener(v -> {
                // Contact info is already displayed above
                android.widget.Toast.makeText(view.getContext(),
                    "Use the contact details above to reach out!",
                    android.widget.Toast.LENGTH_SHORT).show();
            });
        } else {
            btnContact.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
