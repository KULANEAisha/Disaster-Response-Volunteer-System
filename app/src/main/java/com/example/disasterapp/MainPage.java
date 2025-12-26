package com.example.disasterapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainPage extends AppCompatActivity {

    private static final String TAG = "MainPage";

    private RecyclerView recyclerView;
    private EmergencyAdapter adapter;
    private DatabaseReference emergenciesDatabase;
    private DatabaseReference volunteersDatabase;
    private List<EmergencyRequest> emergencyList;
    private List<EmergencyRequest> originalEmergencyList;
    private EditText searchEditText;
    private TextView activeAlertsCount, volunteersActiveCount;
    private LinearLayout viewMatchedButton; // Changed from Button to LinearLayout
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);

        // Initialize Firebase
        emergenciesDatabase = FirebaseDatabase.getInstance().getReference("emergencies");
        volunteersDatabase = FirebaseDatabase.getInstance().getReference("Volunteers");

        // Initialize views
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerView);
        activeAlertsCount = findViewById(R.id.activeAlertsCount);
        volunteersActiveCount = findViewById(R.id.volunteersActiveCount);
        viewMatchedButton = findViewById(R.id.viewMatchedButton); // Now references LinearLayout

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emergencyList = new ArrayList<>();
        originalEmergencyList = new ArrayList<>();
        adapter = new EmergencyAdapter(this, emergencyList);
        recyclerView.setAdapter(adapter);

        // Load data from Firebase
        loadEmergencyRequests();
        loadVolunteersCount();

        // Setup search functionality
        setupSearch();

        // Setup matched emergencies button
        setupMatchedButton();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void loadEmergencyRequests() {
        emergenciesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                originalEmergencyList.clear();

                Log.d(TAG, "Total emergencies in Firebase: " + snapshot.getChildrenCount());

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        EmergencyRequest request = data.getValue(EmergencyRequest.class);
                        if (request != null) {
                            request.setId(data.getKey());
                            originalEmergencyList.add(request);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing emergency request: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // Update active alerts count
                activeAlertsCount.setText(String.valueOf(originalEmergencyList.size()));

                Log.d(TAG, "Loaded " + originalEmergencyList.size() + " emergencies");

                // Apply any existing search filter
                applySearchFilter();

                if (originalEmergencyList.isEmpty()) {
                    Toast.makeText(MainPage.this, "No emergencies found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load data: " + error.getMessage());
                Toast.makeText(MainPage.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVolunteersCount() {
        volunteersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int availableVolunteers = 0;

                for (DataSnapshot volunteerSnapshot : snapshot.getChildren()) {
                    // Check if volunteer is available
                    Boolean isAvailable = volunteerSnapshot.child("isAvailable").getValue(Boolean.class);

                    // Count all volunteers, or only available ones
                    // Option 1: Count only available volunteers
                    if (isAvailable != null && isAvailable) {
                        availableVolunteers++;
                    } else if (isAvailable == null) {
                        // If isAvailable is not set, assume available (for backward compatibility)
                        availableVolunteers++;
                    }

                    // Option 2: Count all volunteers regardless of availability
                    // availableVolunteers++;
                }

                volunteersActiveCount.setText(String.valueOf(availableVolunteers));
                Log.d(TAG, "Available volunteers: " + availableVolunteers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load volunteers: " + error.getMessage());
                volunteersActiveCount.setText("0");
            }
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applySearchFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Applies search filter to the original list
     * Filters by: Emergency Type, Urgency Level, and Location
     */
    private void applySearchFilter() {
        emergencyList.clear();

        // If search is empty, show all items
        if (currentSearchQuery.isEmpty()) {
            emergencyList.addAll(originalEmergencyList);
        } else {
            String filterPattern = currentSearchQuery.toLowerCase();

            for (EmergencyRequest request : originalEmergencyList) {
                boolean matchFound = false;

                // Filter by Emergency Type
                if (request.getType() != null &&
                        request.getType().toLowerCase().contains(filterPattern)) {
                    matchFound = true;
                }

                // Filter by Urgency Level (Low, Medium, High, Critical)
                if (!matchFound && request.getUrgency() != null &&
                        request.getUrgency().toLowerCase().contains(filterPattern)) {
                    matchFound = true;
                }

                // Filter by Location
                if (!matchFound && request.getLocation() != null &&
                        request.getLocation().toLowerCase().contains(filterPattern)) {
                    matchFound = true;
                }

                // Optionally: Filter by Description
                if (!matchFound && request.getDescription() != null &&
                        request.getDescription().toLowerCase().contains(filterPattern)) {
                    matchFound = true;
                }

                if (matchFound) {
                    emergencyList.add(request);
                }
            }
        }

        adapter.notifyDataSetChanged();
        Log.d(TAG, "Filtered results: " + emergencyList.size() + " emergencies");
    }

    private void setupMatchedButton() {
        viewMatchedButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MatchedEmergenciesActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_alerts);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_alerts) {
                return true; // This is the main page
            } else if (itemId == R.id.nav_updates) {
                startActivity(new Intent(getApplicationContext(), UpdateActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_requests) {
                startActivity(new Intent(getApplicationContext(), Request.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}