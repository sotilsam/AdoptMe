package com.example.adoptme;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PetsAdapter extends RecyclerView.Adapter<PetsAdapter.PetViewHolder> {

    private List<Pet> petList;

    public PetsAdapter(List<Pet> petList) {
        this.petList = petList;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);


        holder.tvName.setText(pet.getName());

        String subtitle = pet.getBreed() + " • " + pet.getGender();
        holder.tvBreed.setText(subtitle);


        String info = pet.getAgeCategory() + " • " + pet.getSize() + " • " + pet.getLocation();
        holder.tvInfo.setText(info);


        if (pet.getImageUrl() != null && !pet.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(pet.getImageUrl())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivImage);
        }
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBreed, tvInfo;
        ImageView ivImage;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPetName);
            tvBreed = itemView.findViewById(R.id.tvPetBreed);
            tvInfo = itemView.findViewById(R.id.tvPetInfo);
            ivImage = itemView.findViewById(R.id.ivPetImage);
        }

    }

}