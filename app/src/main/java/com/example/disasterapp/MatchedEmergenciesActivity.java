package com.example.disasterapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button; // Added for refresh button logic, if needed
import android.widget.LinearLayout; // Added for new layout views
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // Added for CardView imports
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

public class MatchedEmergenciesActivity extends AppCompatActivity {

    private static final String TAG = "MatchedEmergencies";

    private RecyclerView matchedRecyclerView;
    private TextView emptyStateTextView, bestMatchTextView; // Kept existing
    private TextView matchCountTextView; // Kept existing, but logic is simplified below

    // New/Updated View Declarations
    private TextView matchCountNumber, bestMatchScore;
    private LinearLayout sectionHeader, loadingLayout, emptyStateLayout; // emptyStateLayout is assumed from your update logic
    private CardView bestMatchBanner, backButton;
    private ProgressBar progressBar; // Kept existing

    private MatchedEmergenciesAdapter adapter;
    private List<EmergencyMatcher.EmergencyMatch> matchedEmergencies;

    private DatabaseReference emergenciesRef, volunteerRef;
    private FirebaseAuth mAuth;
    private String userId;

    private Volunteer currentVolunteer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched_emergencies);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        emergenciesRef = FirebaseDatabase.getInstance().getReference("emergencies");
        volunteerRef = FirebaseDatabase.getInstance().getReference("Volunteers").child(userId);

        // Initialize views
        initializeViews();

        // Load data
        loadVolunteerDataAndMatch();
    }

    private void initializeViews() {
        matchedRecyclerView = findViewById(R.id.matchedRecyclerView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        bestMatchTextView = findViewById(R.id.bestMatchTextView);
        matchCountTextView = findViewById(R.id.matchCountTextView); // Kept for consistency, but may be redundant

        // Initialize new/updated views
        matchCountNumber = findViewById(R.id.matchCountNumber);
        bestMatchScore = findViewById(R.id.bestMatchScore);
        sectionHeader = findViewById(R.id.sectionHeader);
        loadingLayout = findViewById(R.id.loadingLayout); // Assumed new loading container
        bestMatchBanner = findViewById(R.id.bestMatchBanner);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar); // Still used for loading state


        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        matchedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        matchedEmergencies = new ArrayList<>();
        adapter = new MatchedEmergenciesAdapter(this, matchedEmergencies);
        matchedRecyclerView.setAdapter(adapter);

        // Back button click listener
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Setup refresh button logic (if R.id.refreshButton exists)
        setupRefreshButton();
    }

    private void loadVolunteerDataAndMatch() {
        // Use the new loading layout logic
        loadingLayout.setVisibility(View.VISIBLE);
        matchedRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE); // Ensure it's hidden during load
        sectionHeader.setVisibility(View.GONE);
        bestMatchBanner.setVisibility(View.GONE);


        volunteerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentVolunteer = snapshot.getValue(Volunteer.class);
                    if (currentVolunteer != null) {
                        currentVolunteer.setUserId(userId);

                        // Check if volunteer has skills
                        if (currentVolunteer.getSkills() == null || currentVolunteer.getSkills().isEmpty()) {
                            showError("Please add your skills in your profile to see matched emergencies");
                            return;
                        }

                        loadEmergenciesAndMatch();
                    } else {
                        showError("Unable to load your profile");
                    }
                } else {
                    showError("Profile not found. Please complete your profile.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Error loading profile: " + error.getMessage());
            }
        });
    }

    private void loadEmergenciesAndMatch() {
        // Changed to addValueEventListener for real-time updates (like original code)
        // If you only want a single load, change it to addListenerForSingleValueEvent
        emergenciesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<EmergencyRequest> allEmergencies = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    EmergencyRequest emergency = data.getValue(EmergencyRequest.class);
                    if (emergency != null) {
                        emergency.setId(data.getKey());
                        allEmergencies.add(emergency);
                    }
                }

                // Perform matching
                if (!allEmergencies.isEmpty()) {
                    matchEmergencies(allEmergencies);
                } else {
                    showEmptyState("No emergencies available at the moment");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Error loading emergencies: " + error.getMessage());
            }
        });
    }

    private void matchEmergencies(List<EmergencyRequest> emergencies) {
        matchedEmergencies.clear();
        List<EmergencyMatcher.EmergencyMatch> matches = EmergencyMatcher.matchVolunteerToEmergencies(
                currentVolunteer, emergencies
        );

        // Filter by minimum score (35% match or higher)
        List<EmergencyMatcher.EmergencyMatch> filteredMatches = EmergencyMatcher.filterByMinimumScore(matches, 35.0);
        matchedEmergencies.addAll(filteredMatches);

        // Update UI using the new logic for showing matches
        showMatches(matchedEmergencies);
    }

    private void showMatches(List<EmergencyMatcher.EmergencyMatch> matches) {
        // Hide loading
        loadingLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE); // Ensure old progress bar is also hidden

        if (matches.isEmpty()) {
            // Show empty state
            emptyStateLayout.setVisibility(View.VISIBLE);
            matchedRecyclerView.setVisibility(View.GONE);
            sectionHeader.setVisibility(View.GONE);
            bestMatchBanner.setVisibility(View.GONE);

            // Update stats
            matchCountNumber.setText("0");
            bestMatchScore.setText("--");
            emptyStateTextView.setText("No matching emergencies found.\nTry updating your skills or service area!");

        } else {
            // Show matches
            emptyStateLayout.setVisibility(View.GONE);
            matchedRecyclerView.setVisibility(View.VISIBLE);
            sectionHeader.setVisibility(View.VISIBLE);

            // Update stats
            matchCountNumber.setText(String.valueOf(matches.size()));

            // Get best match score
            EmergencyMatcher.EmergencyMatch bestMatch = matches.get(0);
            String matchPercentage = String.format("%.0f%%", bestMatch.getScore());
            bestMatchScore.setText(matchPercentage);

            // Show banner if excellent match (>= 80%)
            if (bestMatch.getScore() >= 80) {
                bestMatchBanner.setVisibility(View.VISIBLE);
                bestMatchTextView.setText(String.format("Excellent match found! %s compatibility with %s.",
                        matchPercentage, bestMatch.getEmergency().getType()));
            } else {
                bestMatchBanner.setVisibility(View.GONE);
            }

            // Set up adapter (only notify changes if adapter is already set and data is cleared/added)
            // Re-setting the adapter is safer if the object itself is used for the list
            adapter.notifyDataSetChanged();

            // Clear out old UI elements if they conflict (from original code)
            matchCountTextView.setVisibility(View.GONE); // Hide original TextView
        }
    }


    private void showEmptyState(String message) {
        loadingLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        matchedRecyclerView.setVisibility(View.GONE);
        sectionHeader.setVisibility(View.GONE);
        bestMatchBanner.setVisibility(View.GONE);

        emptyStateLayout.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(message);

        // Reset stats
        matchCountNumber.setText("0");
        bestMatchScore.setText("--");
    }

    private void showError(String message) {
        loadingLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showEmptyState(message);
    }

    // Helper method for refresh logic
    private void setupRefreshButton() {
        Button refreshButton = findViewById(R.id.refreshButton);
        if (refreshButton != null) {
            refreshButton.setOnClickListener(v -> {
                // Reload matches
                loadingLayout.setVisibility(View.VISIBLE);
                emptyStateLayout.setVisibility(View.GONE);
                matchedRecyclerView.setVisibility(View.GONE);
                sectionHeader.setVisibility(View.GONE);
                bestMatchBanner.setVisibility(View.GONE);

                // Re-trigger the main loading logic
                loadVolunteerDataAndMatch();
            });
        }
    }
}