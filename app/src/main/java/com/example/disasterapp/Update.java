package com.example.disasterapp;

public class Update {
    private String id;
    private String userId;           // ✅ Added to track which user made the update
    private String userName;
    private String userInitial;      // ✅ For letter avatar
    private String description;
    private String imageUrl;
    private String timestamp;
    private long timeMillis;
    private String profileImageUrl;  // ✅ For profile photo

    // ✅ Default constructor required for Firebase
    public Update() {}

    // ✅ Full constructor including all fields
    public Update(String id, String userId, String userName, String userInitial,
                  String description, String imageUrl, String timestamp,
                  long timeMillis, String profileImageUrl) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userInitial = userInitial;
        this.description = description;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.timeMillis = timeMillis;
        this.profileImageUrl = profileImageUrl;
    }

    // ✅ Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserInitial() {
        return userInitial;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // ✅ Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserInitial(String userInitial) {
        this.userInitial = userInitial;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
