package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;

public class MatchFragment extends Fragment {

    public MatchFragment() {
        super(R.layout.fragment_match);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivPet = view.findViewById(R.id.ivMatchPet);
        TextView tvDetails = view.findViewById(R.id.tvMatchDetails);

        // קבלת המידע שהועבר ממסך ה-Explor
        if (getArguments() != null) {
            String petName = getArguments().getString("petName");
            String petImage = getArguments().getString("petImage");

            // עדכון הטקסט והתמונה
            tvDetails.setText("Hello User,\nYou matched with " + petName + "!");

            Glide.with(this)
                    .load(petImage)
                    .centerCrop()
                    .into(ivPet);
        }
    }
}