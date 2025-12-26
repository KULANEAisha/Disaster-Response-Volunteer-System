package com.example.disasterapp;

public class EmergencyRequest {
    private String id;
    private String type;
    private String location;
    private String urgency;
    private String description;
    private String volunteers;
    private String dateTime;
    private String requiredSkills;

    // Default constructor (required for Firebase)
    public EmergencyRequest() {
    }

    // Full constructor
    public EmergencyRequest(String id, String type, String location, String urgency,
                            String description, String volunteers, String dateTime, String requiredSkills) {
        this.id = id;
        this.type = type;
        this.location = location;
        this.urgency = urgency;
        this.description = description;
        this.volunteers = volunteers;
        this.dateTime = dateTime;
        this.requiredSkills = requiredSkills;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public String getUrgency() {
        return urgency;
    }

    public String getDescription() {
        return description;
    }

    public String getVolunteers() {
        return volunteers;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVolunteers(String volunteers) {
        this.volunteers = volunteers;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }
}