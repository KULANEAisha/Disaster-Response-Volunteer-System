package com.example.disasterapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Request extends AppCompatActivity {

    private EditText locationEditText, descriptionEditText, volunteersEditText, dateTimeEditText, requiredSkillsEditText;
    private Spinner emergencyTypeSpinner;
    private Button lowButton, mediumButton, highButton, criticalButton, submitButton;
    private String urgencyLevel = "";
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("emergencies");

        // Initialize views
        locationEditText = findViewById(R.id.locationEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        volunteersEditText = findViewById(R.id.volunteersEditText);
        dateTimeEditText = findViewById(R.id.dateTimeEditText);
        requiredSkillsEditText = findViewById(R.id.requiredSkillsEditText);
        emergencyTypeSpinner = findViewById(R.id.emergencyTypeSpinner);
        lowButton = findViewById(R.id.lowButton);
        mediumButton = findViewById(R.id.mediumButton);
        highButton = findViewById(R.id.highButton);
        criticalButton = findViewById(R.id.criticalButton);
        submitButton = findViewById(R.id.submitButton);

        // Setup components
        setupEmergencyTypeSpinner();
        setupUrgencyButtons();
        setupDateTimePicker();
        setupBottomNavigation();

        submitButton.setOnClickListener(v -> submitEmergencyRequest());
    }

    private void setupEmergencyTypeSpinner() {
        String[] emergencyTypes = {"Fire", "Flood", "Earthquake", "Medical", "Rescue"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, emergencyTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emergencyTypeSpinner.setAdapter(adapter);
    }

    private void setupUrgencyButtons() {
        lowButton.setOnClickListener(v -> setUrgency("Low"));
        mediumButton.setOnClickListener(v -> setUrgency("Medium"));
        highButton.setOnClickListener(v -> setUrgency("High"));
        criticalButton.setOnClickListener(v -> setUrgency("Critical"));
    }

    private void setUrgency(String level) {
        urgencyLevel = level;

        // Reset colors
        lowButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        mediumButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        highButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        criticalButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        // Highlight selected
        switch (level) {
            case "Low":
                lowButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "Medium":
                mediumButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                break;
            case "High":
                highButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "Critical":
                criticalButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                break;
        }
    }

    private void setupDateTimePicker() {
        dateTimeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        TimePickerDialog timePicker = new TimePickerDialog(this,
                                (timeView, selectedHour, selectedMinute) -> {
                                    String dateTime = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear +
                                            " " + selectedHour + ":" + selectedMinute;
                                    dateTimeEditText.setText(dateTime);
                                }, hour, minute, true);
                        timePicker.show();
                    }, year, month, day);
            datePicker.show();
        });
    }

    private void submitEmergencyRequest() {
        String location = locationEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String volunteers = volunteersEditText.getText().toString().trim();
        String dateTime = dateTimeEditText.getText().toString().trim();
        String requiredSkills = requiredSkillsEditText.getText().toString().trim();
        String type = emergencyTypeSpinner.getSelectedItem().toString();

        if (location.isEmpty() || description.isEmpty() || urgencyLevel.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Push data to Firebase
        String id = mDatabase.push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("location", location);
        data.put("description", description);
        data.put("volunteers", volunteers);
        data.put("dateTime", dateTime);
        data.put("requiredSkills", requiredSkills);
        data.put("type", type);
        data.put("urgency", urgencyLevel);

        assert id != null;
        mDatabase.child(id).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Emergency Request Submitted", Toast.LENGTH_SHORT).show();
                clearForm();
            } else {
                Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        locationEditText.setText("");
        descriptionEditText.setText("");
        volunteersEditText.setText("");
        dateTimeEditText.setText("");
        requiredSkillsEditText.setText("");
        emergencyTypeSpinner.setSelection(0);
        urgencyLevel = "";
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Highlight "Request" as red (active)
        bottomNavigationView.setSelectedItemId(R.id.nav_requests);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(getApplicationContext(), MainPage.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_updates) {
                startActivity(new Intent(getApplicationContext(), UpdateActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_requests) {
                // Stay here
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