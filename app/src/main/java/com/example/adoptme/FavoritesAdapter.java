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
        Button btnLocation = dialog.findViewById(R.id.btnFavDetailLocation);
        TextView tvDescription = dialog.findViewById(R.id.tvFavDetailDescription);
        Button btnEmail = dialog.findViewById(R.id.btnFavDetailEmail);
        Button btnPhone = dialog.findViewById(R.id.btnFavDetailPhone);
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
        tvDescription.setText(pet.getDescription() != null ? pet.getDescription() : "No description available");

        // Handle location button
        if (pet.getLocation() != null) {
            btnLocation.setVisibility(View.VISIBLE);
            btnLocation.setOnClickListener(v -> openGoogleMaps(view, pet.getLocation()));
        } else {
            btnLocation.setVisibility(View.GONE);
        }

        // Show contact info if available
        if (pet.getContactEmail() != null && !pet.getContactEmail().isEmpty()) {
            btnEmail.setText("Email: " + pet.getContactEmail());
            btnEmail.setVisibility(View.VISIBLE);
            btnEmail.setOnClickListener(v -> {
                android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                emailIntent.setData(android.net.Uri.parse("mailto:" + pet.getContactEmail()));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Inquiry about " + pet.getName());
                view.getContext().startActivity(android.content.Intent.createChooser(emailIntent, "Send email"));
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
                view.getContext().startActivity(dialIntent);
            });
        } else {
            btnPhone.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openGoogleMaps(View view, com.google.firebase.firestore.GeoPoint location) {
        if (location == null) return;

        String uri = String.format("geo:%f,%f?q=%f,%f",
            location.getLatitude(), location.getLongitude(),
            location.getLatitude(), location.getLongitude());

        android.content.Intent mapIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
            view.getContext().startActivity(mapIntent);
        } else {
            // Fallback to browser if Google Maps not installed
            String browserUri = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f",
                location.getLatitude(), location.getLongitude());
            android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(browserUri));
            view.getContext().startActivity(browserIntent);
        }
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
