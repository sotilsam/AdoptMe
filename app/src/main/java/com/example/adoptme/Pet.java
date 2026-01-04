package com.example.adoptme;

import com.google.firebase.firestore.PropertyName;

public class Pet {
    private String name;
    private String type; // Dog/Cat
    private String breed;
    private String ageCategory; // Puppy, Adult, Senior
    private String gender;      // Male, Female (חדש!)
    private String size;
    private String description;
    private String location;
    private String imageUrl;


    public Pet() {}

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public String getBreed() { return breed; }
    public String getAgeCategory() { return ageCategory; }
    public String getGender() { return gender; }
    public String getSize() { return size; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }


    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setAgeCategory(String ageCategory) { this.ageCategory = ageCategory; }
    public void setGender(String gender) { this.gender = gender; }
    public void setSize(String size) { this.size = size; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
