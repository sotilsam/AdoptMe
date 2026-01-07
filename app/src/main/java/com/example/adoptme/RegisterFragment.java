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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegisterAction);

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // Create user document in Firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("fullName", fullName);
                    user.put("phone", phone);
                    user.put("favorites", new ArrayList<String>());
                    user.put("passed", new ArrayList<String>());

                    // Default preferences
                    Map<String, Object> preferences = new HashMap<>();
                    preferences.put("locationRadius", 30);
                    preferences.put("size", Arrays.asList("Small", "Medium", "Large"));
                    preferences.put("type", Arrays.asList("Cat", "Dog"));
                    user.put("preferences", preferences);

                    db.collection("users").document(uid).set(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                                NavHostFragment.findNavController(this)
                                        .navigate(R.id.action_register_to_explore);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}