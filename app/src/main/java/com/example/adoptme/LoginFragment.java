package com.example.adoptme;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        etEmail = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLoginAction);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> handleLogin());
        }
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign in with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_login_to_explore);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

}