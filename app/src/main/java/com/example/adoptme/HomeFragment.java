package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // This links to your yellow layout (fragment_home.xml)
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Find the Login Button
        Button btnLogin = view.findViewById(R.id.btnLogin);
        // 2. Find the Register Button
        Button btnRegister = view.findViewById(R.id.btnRegister);

        // 3. Navigate to Login
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigate(R.id.loginFragment)
            );
        }

        // 4. Navigate to Register
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigate(R.id.registerFragment)
            );
        }
    }
}
