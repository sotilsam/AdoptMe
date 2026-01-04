package com.example.adoptme;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment {

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvFavorites);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        ArrayList<FavoriteItem> demo = new ArrayList<>();
        demo.add(new FavoriteItem("Pie"));
        demo.add(new FavoriteItem("Yuki"));
        demo.add(new FavoriteItem("Luna"));
        demo.add(new FavoriteItem("Rocky"));

        rv.setAdapter(new FavoritesAdapter(demo, item -> {
            // TODO: open pet details fragment
        }));

        BottomNavHelper.wire(view, this);
    }
}

