package com.example.project;



public class Pet {
    private String name;
    private String type;
    private String ageCategory;
    private String size;
    private String imageUrl;
    private String description;
    private String location;

    // בנאי ריק (חובה בשביל Firebase)
    public Pet() {}

    // --- Setters (החלק שצבע לך את המסך באדום כי הוא היה חסר) ---
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setAgeCategory(String ageCategory) { this.ageCategory = ageCategory; }
    public void setSize(String size) { this.size = size; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }

    // --- Getters (בשביל לשלוף נתונים אחר כך) ---
    public String getName() { return name; }
    public String getType() { return type; }
    public String getAgeCategory() { return ageCategory; }
    public String getSize() { return size; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
}