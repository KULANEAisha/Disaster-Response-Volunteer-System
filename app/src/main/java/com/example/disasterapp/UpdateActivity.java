package com.example.disasterapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpdateActivity extends AppCompatActivity {

    private static final String TAG = "UpdateActivity";

    private EditText updateEditText;
    private Button photoButton, sendButton;
    private RecyclerView updatesRecyclerView;
    private UpdateAdapter adapter;
    private List<Update> updateList;

    private DatabaseReference mDatabase;
    private DatabaseReference userDatabase;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private Uri selectedImageUri;

    private String currentUserName = "User";
    private String currentUserProfileImageUrl = null;
    private String userId;

    // Activity result launcher for picking images
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, redirect to sign in
            startActivity(new Intent(this, Signin.class));
            finish();
            return;
        }

        userId = currentUser.getUid();

        // Initialize Firebase Database and Storage
        mDatabase = FirebaseDatabase.getInstance().getReference("updates");
        userDatabase = FirebaseDatabase.getInstance().getReference("Volunteers").child(userId);
        mStorage = FirebaseStorage.getInstance().getReference("update_images");

        // Initialize views
        updateEditText = findViewById(R.id.updateEditText);
        photoButton = findViewById(R.id.photoButton);
        sendButton = findViewById(R.id.sendButton);
        updatesRecyclerView = findViewById(R.id.updatesRecyclerView);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(false);
        layoutManager.setStackFromEnd(false);
        updatesRecyclerView.setLayoutManager(layoutManager);
        updatesRecyclerView.setHasFixedSize(true);
        updateList = new ArrayList<>();
        adapter = new UpdateAdapter(this, updateList);
        updatesRecyclerView.setAdapter(adapter);

        Log.d(TAG, "RecyclerView setup complete");

        // Setup image picker launcher
        setupImagePicker();

        // Load user data
        loadUserData();

        // Load updates from Firebase
        loadUpdates();

        // Setup button listeners
        photoButton.setOnClickListener(v -> openImagePicker());
        sendButton.setOnClickListener(v -> postUpdate());

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void loadUserData() {
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    if (fullName != null && !fullName.isEmpty()) {
                        currentUserName = fullName;
                    }

                    currentUserProfileImageUrl = profileImageUrl;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading user data: " + databaseError.getMessage());
            }
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void postUpdate() {
        String description = updateEditText.getText().toString().trim();

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please enter update description", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        sendButton.setEnabled(false);
        sendButton.setText("Posting...");

        // Generate update ID
        String updateId = mDatabase.push().getKey();
        if (updateId == null) {
            Toast.makeText(this, "Error creating update", Toast.LENGTH_SHORT).show();
            sendButton.setEnabled(true);
            sendButton.setText("Send");
            return;
        }

        // Get current timestamp
        long currentTimeMillis = System.currentTimeMillis();
        String timestamp = getTimeAgo(currentTimeMillis);

        // If image is selected, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSaveUpdate(updateId, description, timestamp, currentTimeMillis);
        } else {
            saveUpdateToDatabase(updateId, description, null, timestamp, currentTimeMillis);
        }
    }

    private void uploadImageAndSaveUpdate(String updateId, String description, String timestamp, long timeMillis) {
        StorageReference fileRef = mStorage.child(updateId + ".jpg");

        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveUpdateToDatabase(updateId, description, imageUrl, timestamp, timeMillis);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error getting download URL: " + e.getMessage());
                            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
                            sendButton.setEnabled(true);
                            sendButton.setText("Send");
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading image: " + e.getMessage());
                    Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    sendButton.setEnabled(true);
                    sendButton.setText("Send");
                });
    }

    private void saveUpdateToDatabase(String updateId, String description, String imageUrl, String timestamp, long timeMillis) {
        // Get first letter of user's name for avatar
        String avatarLetter = currentUserName.substring(0, 1).toUpperCase();

        // Updated constructor with userId
        Update update = new Update(
                updateId,
                userId,  // Add userId here
                currentUserName,
                avatarLetter,
                description,
                imageUrl,
                timestamp,
                timeMillis,
                currentUserProfileImageUrl
        );

        mDatabase.child(updateId).setValue(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Update posted successfully", Toast.LENGTH_SHORT).show();
                    updateEditText.setText("");
                    selectedImageUri = null;
                    sendButton.setEnabled(true);
                    sendButton.setText("Send");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error posting update: " + e.getMessage());
                    Toast.makeText(this, "Error posting update", Toast.LENGTH_SHORT).show();
                    sendButton.setEnabled(true);
                    sendButton.setText("Send");
                });
    }

    private void loadUpdates() {
        Log.d(TAG, "loadUpdates() called");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange triggered - snapshot exists: " + snapshot.exists());
                Log.d(TAG, "Total updates in Firebase: " + snapshot.getChildrenCount());

                updateList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Log.d(TAG, "Processing update with key: " + data.getKey());

                        Update update = data.getValue(Update.class);
                        if (update != null) {
                            update.setId(data.getKey());

                            // Recalculate time ago for each update
                            if (update.getTimeMillis() > 0) {
                                update.setTimestamp(getTimeAgo(update.getTimeMillis()));
                            }

                            updateList.add(update);
                            Log.d(TAG, "Added update #" + updateList.size() + ": " +
                                    update.getUserName() + " - " + update.getDescription());
                        } else {
                            Log.e(TAG, "Update is null for key: " + data.getKey());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing update: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // Sort by timestamp (most recent first)
                if (updateList.size() > 1) {
                    updateList.sort((u1, u2) -> Long.compare(u2.getTimeMillis(), u1.getTimeMillis()));
                }

                Log.d(TAG, "Final update list size: " + updateList.size());
                Log.d(TAG, "Calling adapter.notifyDataSetChanged()");

                // Force update the adapter
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter item count after notify: " + adapter.getItemCount());
                });

                Log.d(TAG, "Adapter item count: " + adapter.getItemCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load updates: " + error.getMessage());
                Toast.makeText(UpdateActivity.this, "Failed to load updates", Toast.LENGTH_SHORT).show();
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

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_updates);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(getApplicationContext(), MainPage.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_updates) {
                return true; // This is the update page
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