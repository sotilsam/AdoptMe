package com.example.adoptme;


import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PetsAdapter adapter;
    private List<Pet> petList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. אתחול ה-RecyclerView
        recyclerView = findViewById(R.id.rvPets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        petList = new ArrayList<>();
        adapter = new PetsAdapter(petList);
        recyclerView.setAdapter(adapter);

        // 2. חיבור ל-Firebase
        db = FirebaseFirestore.getInstance();

        // 3. הבאת הנתונים
        fetchPetsFromFirebase();

    }

    private void fetchPetsFromFirebase() {
        // "לך לתיקיית pets ותביא את כל המסמכים"
        db.collection("pets")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // ניקוי הרשימה הישנה (אם הייתה)
                    petList.clear();

                    // המרה של כל מסמך לאובייקט Pet
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Pet> pets = queryDocumentSnapshots.toObjects(Pet.class);
                        petList.addAll(pets);
                        // עדכון המסך

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "לא נמצאו חיות במאגר", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בטעינת נתונים: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}



