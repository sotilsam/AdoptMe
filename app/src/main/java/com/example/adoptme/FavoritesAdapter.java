package com.example.adoptme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.VH> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteItem item);
    }

    private final List<FavoriteItem> items;
    private final OnFavoriteClickListener listener;

    public FavoritesAdapter(List<FavoriteItem> items, OnFavoriteClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FavoriteItem item = items.get(position);
        h.tvName.setText(item.name);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}

