package com.example.project;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DogApiService {
    // הבקשה: תביא לי רשימה של כלבים, תגביל ל-20 תוצאות
    @GET("v1/breeds")
    Call<List<DogApiResult>> getBreeds(@Query("limit") int limit);
}
