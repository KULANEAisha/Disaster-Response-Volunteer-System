package com.example.disasterapp;

import java.util.ArrayList;
import java.util.List;

public class Volunteer {
    private String userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String serviceArea;
    private double radius; // in kilometers
    private List<String> skills;
    private List<String> availability;
    private double latitude;
    private double longitude;
    private boolean isAvailable;

    // Empty constructor for Firebase
    public Volunteer() {
        skills = new ArrayList<>();
        availability = new ArrayList<>();
        isAvailable = true;
    }

    public Volunteer(String userId, String fullName, String email, String phoneNumber,
                     String serviceArea, double radius, List<String> skills,
                     List<String> availability) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.serviceArea = serviceArea;
        this.radius = radius;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.availability = availability != null ? availability : new ArrayList<>();
        this.isAvailable = true;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getAvailability() {
        return availability;
    }

    public void setAvailability(List<String> availability) {
        this.availability = availability;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
