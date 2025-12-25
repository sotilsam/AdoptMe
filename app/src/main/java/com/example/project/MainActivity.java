package com.example.project;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        private void fetchDogsAndSaveToFirebase() {
            // 1. הגדרת Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.thedogapi.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            DogApiService service = retrofit.create(DogApiService.class);

            // 2. ביצוע הבקשה
            service.getBreeds(20).enqueue(new Callback<List<DogApiResult>>() {
                @Override
                public void onResponse(Call<List<DogApiResult>> call, Response<List<DogApiResult>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<DogApiResult> apiDogs = response.body();

                        // לולאה שעוברת על כל הכלבים שהגיעו מהאינטרנט
                        for (DogApiResult apiDog : apiDogs) {
                            uploadToFirebase(apiDog); // מיד נכתוב את הפונקציה הזו
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<DogApiResult>> call, Throwable t) {
                    Log.e("API_ERROR", "Error fetching dogs", t);
                }
            });
        }

// פונקציית עזר שמקבלת כלב מה-API ושומרת אותו בפורמט שלך ב-Firebase
        private void uploadToFirebase(DogApiResult apiDog) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // בדיקה שיש תמונה (לפעמים ה-API מחזיר כלבים בלי תמונה)
            if (apiDog.image == null || apiDog.image.url == null) return;

            // יצירת האובייקט שלך (Pet)
            // אנחנו "ממציאים" חלק מהנתונים כי ה-API לא מספק אותם, אבל התמונות אמיתיות!
            Pet myPet = new Pet();
            myPet.setName(apiDog.name);
            myPet.setType("dog"); // קבוע
            myPet.setImageUrl(apiDog.image.url); // הכתובת האמיתית מה-API
            myPet.setDescription(apiDog.temperament); // תיאור אופי אמיתי

            // הגרלת נתונים (כדי שיהיה לך גיוון לסינונים)
            myPet.setAgeCategory(Math.random() > 0.5 ? "adult" : "puppy");
            myPet.setSize(Math.random() > 0.5 ? "medium" : "large");
            myPet.setLocation("תל אביב");

            // שמירה
            db.collection("pets").add(myPet);
        }
    }
}