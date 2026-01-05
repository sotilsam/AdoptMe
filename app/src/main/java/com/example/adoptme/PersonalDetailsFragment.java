package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class PersonalDetailsFragment extends Fragment {

    public PersonalDetailsFragment() {
        super(R.layout.fragment_personal_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView title = view.findViewById(R.id.tvTitle);
        title.setText("Personal details");

        ImageButton back = view.findViewById(R.id.btnBack);
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

    }
}
