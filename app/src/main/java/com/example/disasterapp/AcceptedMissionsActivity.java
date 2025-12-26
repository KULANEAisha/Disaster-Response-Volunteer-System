package com.example.disasterapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AcceptedMissionsActivity extends AppCompatActivity {

    private RecyclerView missionsRecyclerView;
    private ProgressBar progressBar;
    private View emptyStateView;
    private TextView backButton;

    private FirebaseAuth mAuth;
    private DatabaseReference volunteersRef, emergenciesRef;
    private String userId;

    private List<EmergencyRequest> acceptedMissions;
    private EmergencyAdapter adapter;
    private int totalMissions = 0;
    private int loadedMissions = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_missions);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        volunteersRef = FirebaseDatabase.getInstance().getReference("Volunteers");
        emergenciesRef = FirebaseDatabase.getInstance().getReference("emergencies"); // Fixed: lowercase

        // Initialize views
        missionsRecyclerView = findViewById(R.id.acceptedMissionsRecyclerView);
        progressBar = findViewById(R.id.acceptedMissionsProgressBar);
        emptyStateView = findViewById(R.id.acceptedMissionsEmptyState);
        backButton = findViewById(R.id.acceptedMissionsBackButton);

        // Setup RecyclerView
        acceptedMissions = new ArrayList<>();
        adapter = new EmergencyAdapter(this, acceptedMissions);
        missionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        missionsRecyclerView.setAdapter(adapter);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Load accepted missions
        loadAcceptedMissions();
    }

    private void loadAcceptedMissions() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);
        missionsRecyclerView.setVisibility(View.GONE);

        volunteersRef.child(userId).child("acceptedMissions")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        acceptedMissions.clear();

                        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                            progressBar.setVisibility(View.GONE);
                            showEmptyState();
                            return;
                        }

                        totalMissions = (int) dataSnapshot.getChildrenCount();
                        loadedMissions = 0;

                        for (DataSnapshot missionSnapshot : dataSnapshot.getChildren()) {
                            String emergencyId = missionSnapshot.child("emergencyId").getValue(String.class);

                            // Fetch full emergency details from emergencies node (lowercase)
                            if (emergencyId != null) {
                                loadFullEmergencyDetails(emergencyId);
                            } else {
                                loadedMissions++;
                                checkIfAllLoaded();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        showEmptyState();
                        Toast.makeText(AcceptedMissionsActivity.this,
                                "Error loading missions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadFullEmergencyDetails(String emergencyId) {
        emergenciesRef.child(emergencyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Based on your Firebase structure (from screenshot)
                    String type = snapshot.child("type").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String urgency = snapshot.child("urgency").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String volunteers = snapshot.child("volunteers").getValue(String.class);
                    String requiredSkills = snapshot.child("requiredSkills").getValue(String.class);
                    String dateTime = snapshot.child("dateTime").getValue(String.class);

                    // Debug logging
                    android.util.Log.d("AcceptedMissions", "Emergency ID: " + emergencyId);
                    android.util.Log.d("AcceptedMissions", "Type: " + type);
                    android.util.Log.d("AcceptedMissions", "Location: " + location);
                    android.util.Log.d("AcceptedMissions", "Urgency: " + urgency);

                    // Create EmergencyRequest with full details
                    EmergencyRequest request = new EmergencyRequest();
                    request.setId(emergencyId);
                    request.setType(type);
                    request.setLocation(location);
                    request.setUrgency(urgency);
                    request.setDescription(description);
                    request.setVolunteers(volunteers);
                    request.setRequiredSkills(requiredSkills);
                    request.setDateTime(dateTime);

                    acceptedMissions.add(request);
                } else {
                    android.util.Log.e("AcceptedMissions", "Emergency not found: " + emergencyId);
                }

                loadedMissions++;
                checkIfAllLoaded();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                android.util.Log.e("AcceptedMissions", "Error loading emergency: " + error.getMessage());
                loadedMissions++;
                checkIfAllLoaded();
            }
        });
    }

    private void checkIfAllLoaded() {
        if (loadedMissions >= totalMissions) {
            progressBar.setVisibility(View.GONE);

            if (acceptedMissions.isEmpty()) {
                showEmptyState();
            } else {
                showMissions();
            }
        }
    }

    private void showEmptyState() {
        emptyStateView.setVisibility(View.VISIBLE);
        missionsRecyclerView.setVisibility(View.GONE);
    }

    private void showMissions() {
        emptyStateView.setVisibility(View.GONE);
        missionsRecyclerView.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }
}