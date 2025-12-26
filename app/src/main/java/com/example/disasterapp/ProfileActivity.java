package com.example.disasterapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, statusTextView, missionsTextView, updatesTextView;
    private TextView serviceAreaTextView, availabilityStatusText;
    private ChipGroup skillsChipGroup;
    private LinearLayout availabilityContainer;
    private MaterialButton addSkillsButton, updateLocationButton;
    private LinearLayout viewMatchedButton; // Changed from MaterialButton to LinearLayout
    private SwitchMaterial availabilitySwitch;
    private View missionsCard;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference updatesReference;
    private String userId;
    private String currentUserName;
    private boolean isAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, Signin.class));
            finish();
            return;
        }

        userId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Volunteers").child(userId);
        updatesReference = FirebaseDatabase.getInstance().getReference("updates");

        initializeViews();
        loadUserData();
        setupButtons();
        setupBottomNavigation();
        setupMissionsClickListener();
        setupUpdatesClickListener();
        setupAvailabilityToggle();
    }

    private void initializeViews() {
        nameTextView = findViewById(R.id.profileNameTextView);
        statusTextView = findViewById(R.id.profileStatusTextView);
        missionsTextView = findViewById(R.id.profileMissionsTextView);
        updatesTextView = findViewById(R.id.profileUpdatesTextView);

        skillsChipGroup = findViewById(R.id.profileSkillsChipGroup);
        availabilityContainer = findViewById(R.id.profileAvailabilityContainer);
        serviceAreaTextView = findViewById(R.id.profileServiceAreaTextView);

        addSkillsButton = findViewById(R.id.profileAddSkillsButton);
        updateLocationButton = findViewById(R.id.profileUpdateLocationButton);
        viewMatchedButton = findViewById(R.id.viewMatchedButton); // Now references LinearLayout

        availabilitySwitch = findViewById(R.id.availabilitySwitch);
        availabilityStatusText = findViewById(R.id.availabilityStatusText);
    }

    private void setupAvailabilityToggle() {
        availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only update if user manually changed it (not from loading data)
            if (buttonView.isPressed()) {
                isAvailable = isChecked;
                updateAvailabilityStatus(isChecked);
            }
        });
    }

    private void updateAvailabilityStatus(boolean available) {
        // Update UI
        if (available) {
            statusTextView.setText("Available Now");
            statusTextView.setTextColor(Color.WHITE);
            statusTextView.getParent().requestLayout(); // Refresh parent CardView
            availabilityStatusText.setText("Available for missions");
        } else {
            statusTextView.setText("Unavailable");
            statusTextView.setTextColor(Color.WHITE);
            statusTextView.getParent().requestLayout();
            availabilityStatusText.setText("Not available for missions");
        }

        // Save to Firebase
        databaseReference.child("isAvailable").setValue(available)
                .addOnSuccessListener(aVoid -> {
                    String message = available ? "You are now available" : "You are now unavailable";
                    Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                    // Revert switch if failed
                    availabilitySwitch.setChecked(!available);
                });
    }

    private void setupMissionsClickListener() {
        missionsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AcceptedMissionsActivity.class);
            startActivity(intent);
        });
    }

    private void setupUpdatesClickListener() {
        updatesTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UserUpdatesActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String serviceArea = dataSnapshot.child("serviceArea").getValue(String.class);
                    Integer radius = dataSnapshot.child("radius").getValue(Integer.class);
                    Boolean available = dataSnapshot.child("isAvailable").getValue(Boolean.class);

                    currentUserName = fullName;
                    nameTextView.setText(fullName != null ? fullName : "User");
                    serviceAreaTextView.setText(serviceArea != null ? serviceArea : "Not specified");

                    // Set availability status
                    if (available != null) {
                        isAvailable = available;
                        availabilitySwitch.setChecked(available);
                        if (available) {
                            statusTextView.setText("Available Now");
                            availabilityStatusText.setText("Available for missions");
                        } else {
                            statusTextView.setText("Unavailable");
                            availabilityStatusText.setText("Not available for missions");
                        }
                    } else {
                        // Default to available if not set
                        statusTextView.setText("Available Now");
                        availabilityStatusText.setText("Available for missions");
                        availabilitySwitch.setChecked(true);
                    }

                    // Count accepted missions
                    long acceptedCount = 0;
                    if (dataSnapshot.child("acceptedMissions").exists()) {
                        acceptedCount = dataSnapshot.child("acceptedMissions").getChildrenCount();
                    }
                    missionsTextView.setText(String.valueOf(acceptedCount));

                    loadUpdatesCount();
                    loadSkills(dataSnapshot);
                    loadAvailability(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUpdatesCount() {
        updatesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int userUpdatesCount = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Update update = data.getValue(Update.class);
                        if (update != null) {
                            boolean isUserUpdate = false;

                            if (update.getUserId() != null && update.getUserId().equals(userId)) {
                                isUserUpdate = true;
                            } else if (update.getUserId() == null && currentUserName != null
                                    && update.getUserName() != null
                                    && update.getUserName().equals(currentUserName)) {
                                isUserUpdate = true;
                            }

                            if (isUserUpdate) {
                                userUpdatesCount++;
                            }
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }

                updatesTextView.setText(String.valueOf(userUpdatesCount));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                updatesTextView.setText("0");
            }
        });
    }

    private void loadSkills(DataSnapshot dataSnapshot) {
        skillsChipGroup.removeAllViews();

        Object skillsObj = dataSnapshot.child("skills").getValue();
        if (skillsObj instanceof List) {
            List<String> skills = (List<String>) skillsObj;
            for (String skill : skills) {
                addSkillChip(skill);
            }
        }
    }

    private void addSkillChip(String skillText) {
        Chip chip = new Chip(this);
        chip.setText(skillText);
        chip.setChipBackgroundColorResource(R.color.skill_chip_background);
        chip.setTextColor(Color.parseColor("#4338ca"));
        chip.setClickable(false);
        chip.setCheckable(false);
        skillsChipGroup.addView(chip);
    }

    private void loadAvailability(DataSnapshot dataSnapshot) {
        availabilityContainer.removeAllViews();

        Object availObj = dataSnapshot.child("availability").getValue();
        if (availObj instanceof List) {
            List<String> availability = (List<String>) availObj;
            for (String avail : availability) {
                addAvailabilityItem(avail, true);
            }
        }
    }

    private void addAvailabilityItem(String text, boolean isChecked) {
        View itemView = getLayoutInflater().inflate(R.layout.availability_item, availabilityContainer, false);
        TextView textView = itemView.findViewById(R.id.availabilityText);
        View checkIcon = itemView.findViewById(R.id.availabilityCheck);

        textView.setText(text);
        checkIcon.setVisibility(isChecked ? View.VISIBLE : View.GONE);

        availabilityContainer.addView(itemView);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(getApplicationContext(), MainPage.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_updates) {
                startActivity(new Intent(getApplicationContext(), UpdateActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            } else if (itemId == R.id.nav_requests) {
                startActivity(new Intent(getApplicationContext(), Request.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void setupButtons() {
        addSkillsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Add skills feature coming soon", Toast.LENGTH_SHORT).show();
        });

        updateLocationButton.setOnClickListener(v -> {
            Toast.makeText(this, "Update location feature coming soon", Toast.LENGTH_SHORT).show();
        });

        viewMatchedButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MatchedEmergenciesActivity.class);
            startActivity(intent);
        });
    }
}