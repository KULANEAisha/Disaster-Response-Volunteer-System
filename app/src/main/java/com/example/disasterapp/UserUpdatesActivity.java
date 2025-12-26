package com.example.disasterapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserUpdatesActivity extends AppCompatActivity {

    private RecyclerView updatesRecyclerView;
    private ProgressBar progressBar;
    private View emptyStateView;
    private TextView backButton;

    private FirebaseAuth mAuth;
    private DatabaseReference updatesRef;
    private DatabaseReference volunteersRef;
    private String userId;
    private String currentUserName;

    private List<Update> userUpdatesList;
    private UpdateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_updates);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        updatesRef = FirebaseDatabase.getInstance().getReference("updates");

        // Initialize views
        updatesRecyclerView = findViewById(R.id.userUpdatesRecyclerView);
        progressBar = findViewById(R.id.userUpdatesProgressBar);
        emptyStateView = findViewById(R.id.userUpdatesEmptyState);
        backButton = findViewById(R.id.userUpdatesBackButton);

        // Setup RecyclerView
        userUpdatesList = new ArrayList<>();
        adapter = new UpdateAdapter(this, userUpdatesList);
        updatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updatesRecyclerView.setAdapter(adapter);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Load user's updates
        loadUserUpdates();
    }

    private void loadUserUpdates() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);
        updatesRecyclerView.setVisibility(View.GONE);

        updatesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userUpdatesList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Update update = data.getValue(Update.class);
                        if (update != null) {
                            update.setId(data.getKey());

                            // Only add updates from the current user (filter by userId)
                            if (update.getUserId() != null && update.getUserId().equals(userId)) {
                                // Recalculate time ago
                                if (update.getTimeMillis() > 0) {
                                    update.setTimestamp(getTimeAgo(update.getTimeMillis()));
                                }

                                userUpdatesList.add(0, update);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("UserUpdates", "Error parsing update: " + e.getMessage());
                    }
                }

                progressBar.setVisibility(View.GONE);

                if (userUpdatesList.isEmpty()) {
                    showEmptyState();
                } else {
                    showUpdates();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                showEmptyState();
                Toast.makeText(UserUpdatesActivity.this,
                        "Error loading updates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTimeAgo(long timeMillis) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timeMillis;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(new Date(timeMillis));
        }
    }

    private void showEmptyState() {
        emptyStateView.setVisibility(View.VISIBLE);
        updatesRecyclerView.setVisibility(View.GONE);
    }

    private void showUpdates() {
        emptyStateView.setVisibility(View.GONE);
        updatesRecyclerView.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }
}