package com.example.adoptme;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public class Pet {
    private String name;
    private String type; // "Cat" or "Dog"
    private String breed;
    private String ageCategory; // e.g. "Adult", "Puppy", "Kitten", etc.
    private String gender; // e.g. "Male", "Female", or other
    private String size; // "Small", "Medium", or "Large"
    private String description;
    private GeoPoint location; // GeoPoint with latitude and longitude
    private String imageUrl;
    private String contactEmail;
    private String contactPhone;

    private String id;

    public Pet() {}

    public String getName() { return name; }
    public String getType() { return type; }
    public String getBreed() { return breed; }
    public String getAgeCategory() { return ageCategory; }
    public String getGender() { return gender; }
    public String getSize() { return size; }
    public String getDescription() { return description; }
    public GeoPoint getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }

    @Exclude
    public String getId() { return id; }

    @Exclude
    public String getLocationString() {
        if (location != null) {
            return String.format("%.4f, %.4f", location.getLatitude(), location.getLongitude());
        }
        return "Unknown";
    }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setBreed(String breed) { this.breed = breed; }
    public void setAgeCategory(String ageCategory) { this.ageCategory = ageCategory; }
    public void setGender(String gender) { this.gender = gender; }
    public void setSize(String size) { this.size = size; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(GeoPoint location) { this.location = location; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public void setId(String id) { this.id = id; }
}
