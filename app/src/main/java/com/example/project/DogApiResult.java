package com.example.project;

import com.google.gson.annotations.SerializedName;

public class DogApiResult {
    // אנחנו צריכים רק את השם, האופי והתמונה
    @SerializedName("name")
    public String name;

    @SerializedName("temperament")
    public String temperament;

    @SerializedName("image")
    public DogImage image;

    // מחלקה פנימית שמייצגת את אובייקט התמונה ב-JSON
    public class DogImage {
        @SerializedName("url")
        public String url;
    }
}