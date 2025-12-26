package com.example.disasterapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Signup extends AppCompatActivity {

    private TextInputEditText fullNameEditText, emailEditText, passwordEditText, serviceAreaEditText;
    private SeekBar radiusSeekBar;
    private TextView radiusValueTextView, loginTextView;
    private MaterialButton completeRegistrationButton;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    // Skills and Availability
    private List<MaterialButton> skillButtons = new ArrayList<>();
    private List<MaterialButton> availabilityButtons = new ArrayList<>();
    private List<String> selectedSkills = new ArrayList<>();
    private List<String> selectedAvailability = new ArrayList<>();
    private int selectedRadius = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Volunteers");

        initializeViews();
        setupSeekBar();
        setupSkillsButtons();
        setupAvailabilityButtons();
        setupCompleteButton();
        setupLoginLink();
    }

    private void initializeViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        serviceAreaEditText = findViewById(R.id.serviceAreaEditText);

        radiusSeekBar = findViewById(R.id.radiusSeekBar);
        radiusValueTextView = findViewById(R.id.radiusValueTextView);

        completeRegistrationButton = findViewById(R.id.completeRegistrationButton);
        loginTextView = findViewById(R.id.loginTextView);

        // Skills
        skillButtons.add(findViewById(R.id.skillMedical));
        skillButtons.add(findViewById(R.id.skillSearchRescue));
        skillButtons.add(findViewById(R.id.skillFoodDistribution));
        skillButtons.add(findViewById(R.id.skillShelterManagement));
        skillButtons.add(findViewById(R.id.skillTransportation));
        skillButtons.add(findViewById(R.id.skillCommunication));
        skillButtons.add(findViewById(R.id.skillTechnicalSupport));

        // Availability
        availabilityButtons.add(findViewById(R.id.availWeekdayMornings));
        availabilityButtons.add(findViewById(R.id.availWeekdayAfternoons));
        availabilityButtons.add(findViewById(R.id.availWeekdayEvenings));
        availabilityButtons.add(findViewById(R.id.availWeekendDays));
        availabilityButtons.add(findViewById(R.id.availWeekendEvenings));
        availabilityButtons.add(findViewById(R.id.availOvernight));
        availabilityButtons.add(findViewById(R.id.availOnCall));
    }

    private void setupSeekBar() {
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                selectedRadius = progress;
                radiusValueTextView.setText(progress + " km");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupSkillsButtons() {
        for (MaterialButton button : skillButtons) {
            button.setOnClickListener(v -> toggleButton(button, selectedSkills));
        }
    }

    private void setupAvailabilityButtons() {
        for (MaterialButton button : availabilityButtons) {
            button.setOnClickListener(v -> toggleButton(button, selectedAvailability));
        }
    }

    private void toggleButton(MaterialButton button, List<String> selectedList) {
        String value = button.getText().toString();
        if (selectedList.contains(value)) {
            selectedList.remove(value);
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(getResources().getColor(android.R.color.darker_gray));
            button.setStrokeColorResource(android.R.color.darker_gray);
            button.setStrokeWidth(2);
        } else {
            selectedList.add(value);
            button.setBackgroundColor(Color.parseColor("#1e40af"));
            button.setTextColor(Color.WHITE);
            button.setStrokeWidth(0);
        }
    }

    private void setupCompleteButton() {
        completeRegistrationButton.setOnClickListener(v -> handleRegistration());
    }

    private void setupLoginLink() {
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Signup.this, Signin.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegistration() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String serviceArea = serviceAreaEditText.getText().toString().trim();

        if (!validateInputs(fullName, email, password, serviceArea)) return;

        completeRegistrationButton.setEnabled(false);
        completeRegistrationButton.setText("Registering...");

        // Create Firebase user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    completeRegistrationButton.setEnabled(true);
                    completeRegistrationButton.setText("Complete Registration");

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveVolunteerData(user.getUid(), fullName, email, serviceArea);
                        }
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String fullName, String email, String password, String serviceArea) {
        boolean valid = true;

        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError("Full name is required");
            valid = false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            valid = false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Minimum 6 characters");
            valid = false;
        }

        if (TextUtils.isEmpty(serviceArea)) {
            serviceAreaEditText.setError("Service area is required");
            valid = false;
        }

        if (selectedSkills.isEmpty()) {
            Toast.makeText(this, "Select at least one skill", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (selectedAvailability.isEmpty()) {
            Toast.makeText(this, "Select at least one availability", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void saveVolunteerData(String userId, String fullName, String email, String serviceArea) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("serviceArea", serviceArea);
        userData.put("radius", selectedRadius);
        userData.put("skills", selectedSkills);
        userData.put("availability", selectedAvailability);

        databaseReference.child(userId).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Signup.this, MainPage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Error saving data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
