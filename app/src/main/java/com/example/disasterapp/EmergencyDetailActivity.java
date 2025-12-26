package com.example.disasterapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmergencyDetailActivity extends AppCompatActivity {

    private TextView typeTextView, urgencyTextView, locationTextView;
    private TextView descriptionTextView, volunteersTextView, assignedTextView;
    private TextView skillsTextView, dateTimeTextView;
    private Button acceptButton;

    private LinearLayout headerLayout;
    private TextView backButton;

    private FirebaseAuth mAuth;
    private DatabaseReference emergenciesRef, volunteersRef;
    private String emergencyId;
    private String userId;
    private int currentAssigned = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        emergenciesRef = FirebaseDatabase.getInstance().getReference("emergencies"); // Fixed: lowercase
        volunteersRef = FirebaseDatabase.getInstance().getReference("Volunteers");

        // Initialize views
        headerLayout = findViewById(R.id.headerLayout);
        backButton = findViewById(R.id.backButton);
        typeTextView = findViewById(R.id.detailTypeTextView);
        urgencyTextView = findViewById(R.id.detailUrgencyTextView);
        locationTextView = findViewById(R.id.detailLocationTextView);
        descriptionTextView = findViewById(R.id.detailDescriptionTextView);
        volunteersTextView = findViewById(R.id.detailVolunteersTextView);
        assignedTextView = findViewById(R.id.detailAssignedTextView);
        skillsTextView = findViewById(R.id.detailSkillsTextView);
        dateTimeTextView = findViewById(R.id.detailDateTimeTextView);
        acceptButton = findViewById(R.id.acceptMissionButton);

        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Get intent data
        emergencyId = getIntent().getStringExtra("emergencyId");
        String emergencyType = getIntent().getStringExtra("emergencyType");
        String location = getIntent().getStringExtra("location");
        String urgency = getIntent().getStringExtra("urgency");
        String description = getIntent().getStringExtra("description");
        String volunteers = getIntent().getStringExtra("volunteers");
        String requiredSkills = getIntent().getStringExtra("requiredSkills");
        String dateTime = getIntent().getStringExtra("dateTime");

        // Set data to views
        typeTextView.setText(emergencyType != null ? emergencyType : "Unknown");
        locationTextView.setText(location != null ? location : "Unknown location");
        descriptionTextView.setText(description != null ? description : "No description");
        volunteersTextView.setText("Volunteers Needed: " + (volunteers != null ? volunteers : "0"));

        // Set date and time
        if (dateTime != null && !dateTime.isEmpty()) {
            dateTimeTextView.setText("Date & Time: " + dateTime);
        } else {
            dateTimeTextView.setText("Date & Time: Not specified");
        }

        // Set urgency color for badge and header
        setUrgencyColor(urgency);

        // Set Required Skills from Firebase or show default message
        if (requiredSkills != null && !requiredSkills.isEmpty()) {
            skillsTextView.setText(requiredSkills);
        } else {
            skillsTextView.setText("No specific skills required");
        }

        // Load currently assigned count from Firebase
        loadAssignedCount();

        // Check if user already accepted this mission
        checkIfAlreadyAccepted();

        // Accept button click listener
        acceptButton.setOnClickListener(v -> acceptMission(emergencyType, location, urgency));
    }

    private void loadAssignedCount() {
        if (emergencyId != null) {
            emergenciesRef.child(emergencyId).child("assignedVolunteers")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            currentAssigned = 0;
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot volunteer : dataSnapshot.getChildren()) {
                                    currentAssigned++;
                                }
                            }
                            assignedTextView.setText("Currently Assigned: " + currentAssigned);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            assignedTextView.setText("Currently Assigned: 0");
                        }
                    });
        } else {
            assignedTextView.setText("Currently Assigned: 0");
        }
    }

    private void checkIfAlreadyAccepted() {
        if (emergencyId != null) {
            volunteersRef.child(userId).child("acceptedMissions").child(emergencyId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                acceptButton.setEnabled(true);
                                acceptButton.setText("Unaccept Mission");
                                acceptButton.setBackgroundTintList(ColorStateList.valueOf(
                                        getResources().getColor(android.R.color.holo_orange_dark)));

                                // Change click listener to unaccept
                                acceptButton.setOnClickListener(v -> unacceptMission());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle error
                        }
                    });
        }
    }

    private void acceptMission(String emergencyType, String location, String urgency) {
        if (emergencyId == null) {
            Toast.makeText(this, "Error: Emergency ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        acceptButton.setEnabled(false);

        // Get current timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create mission data for volunteer's profile
        Map<String, Object> missionData = new HashMap<>();
        missionData.put("emergencyId", emergencyId);
        missionData.put("emergencyType", emergencyType);
        missionData.put("location", location);
        missionData.put("urgency", urgency);
        missionData.put("acceptedAt", timestamp);
        missionData.put("status", "Active");

        // Create volunteer data for emergency's assigned list
        Map<String, Object> volunteerData = new HashMap<>();
        volunteerData.put("userId", userId);
        volunteerData.put("acceptedAt", timestamp);

        // Update volunteer's accepted missions
        volunteersRef.child(userId).child("acceptedMissions").child(emergencyId)
                .setValue(missionData)
                .addOnSuccessListener(aVoid -> {
                    // Update emergency's assigned volunteers
                    emergenciesRef.child(emergencyId).child("assignedVolunteers").child(userId)
                            .setValue(volunteerData)
                            .addOnSuccessListener(aVoid2 -> {
                                // Increment mission count
                                incrementMissionCount();

                                Toast.makeText(this, "Mission accepted successfully!", Toast.LENGTH_SHORT).show();
                                acceptButton.setText("Unaccept Mission");
                                acceptButton.setBackgroundTintList(ColorStateList.valueOf(
                                        getResources().getColor(android.R.color.holo_orange_dark)));

                                // Enable button and change to unaccept functionality
                                acceptButton.setEnabled(true);
                                acceptButton.setOnClickListener(v -> unacceptMission());
                            })
                            .addOnFailureListener(e -> {
                                acceptButton.setEnabled(true);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    acceptButton.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void unacceptMission() {
        if (emergencyId == null) {
            Toast.makeText(this, "Error: Emergency ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        acceptButton.setEnabled(false);

        // Remove mission from volunteer's accepted missions
        volunteersRef.child(userId).child("acceptedMissions").child(emergencyId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Remove volunteer from emergency's assigned volunteers
                    emergenciesRef.child(emergencyId).child("assignedVolunteers").child(userId)
                            .removeValue()
                            .addOnSuccessListener(aVoid2 -> {
                                // Decrement mission count
                                decrementMissionCount();

                                Toast.makeText(this, "Mission unaccepted successfully!", Toast.LENGTH_SHORT).show();

                                // Reset button to original state
                                acceptButton.setEnabled(true);
                                acceptButton.setText("Accept Mission");
                                acceptButton.setBackgroundTintList(ColorStateList.valueOf(
                                        getResources().getColor(R.color.button_blue)));

                                // Reset click listener to accept
                                String emergencyType = getIntent().getStringExtra("emergencyType");
                                String location = getIntent().getStringExtra("location");
                                String urgency = getIntent().getStringExtra("urgency");
                                acceptButton.setOnClickListener(v -> acceptMission(emergencyType, location, urgency));
                            })
                            .addOnFailureListener(e -> {
                                acceptButton.setEnabled(true);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    acceptButton.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void incrementMissionCount() {
        volunteersRef.child(userId).child("missionsCompleted")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int currentCount = 0;
                        if (dataSnapshot.exists()) {
                            Long count = dataSnapshot.getValue(Long.class);
                            currentCount = count != null ? count.intValue() : 0;
                        }
                        volunteersRef.child(userId).child("missionsCompleted").setValue(currentCount + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void decrementMissionCount() {
        volunteersRef.child(userId).child("missionsCompleted")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int currentCount = 0;
                        if (dataSnapshot.exists()) {
                            Long count = dataSnapshot.getValue(Long.class);
                            currentCount = count != null ? count.intValue() : 0;
                        }
                        // Ensure count doesn't go below 0
                        int newCount = Math.max(0, currentCount - 1);
                        volunteersRef.child(userId).child("missionsCompleted").setValue(newCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void setUrgencyColor(String urgency) {
        int urgencyColor;
        int headerBackgroundColor;

        if (urgency == null || urgency.isEmpty()) {
            urgencyColor = getResources().getColor(android.R.color.darker_gray);
            headerBackgroundColor = getResources().getColor(android.R.color.darker_gray);
        } else if (urgency.equalsIgnoreCase("Low")) {
            urgencyColor = getResources().getColor(android.R.color.holo_green_light);
            headerBackgroundColor = getResources().getColor(android.R.color.holo_green_light);
        } else if (urgency.equalsIgnoreCase("Medium")) {
            urgencyColor = getResources().getColor(android.R.color.holo_orange_light);
            headerBackgroundColor = getResources().getColor(android.R.color.holo_orange_light);
        } else if (urgency.equalsIgnoreCase("High")) {
            urgencyColor = getResources().getColor(android.R.color.holo_orange_dark);
            headerBackgroundColor = getResources().getColor(android.R.color.holo_orange_dark);
        } else if (urgency.equalsIgnoreCase("Critical")) {
            urgencyColor = getResources().getColor(android.R.color.holo_red_light);
            headerBackgroundColor = getResources().getColor(android.R.color.holo_red_light);
        } else {
            urgencyColor = getResources().getColor(android.R.color.darker_gray);
            headerBackgroundColor = getResources().getColor(android.R.color.darker_gray);
        }

        // Set header background to urgency color
        headerLayout.setBackgroundColor(headerBackgroundColor);

        // Set urgency badge with white background and colored text
        urgencyTextView.setText(urgency);
        urgencyTextView.setTextColor(urgencyColor);
        urgencyTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white)));
    }
}