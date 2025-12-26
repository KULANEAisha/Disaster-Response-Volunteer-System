package com.example.disasterapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Signin extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private TextView forgotPasswordTextView, signupTextView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // ✅ Initialize Firebase safely
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // 🔗 Link views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        signupTextView = findViewById(R.id.signupTextView);

        // 🔘 Login button
        loginButton.setOnClickListener(v -> loginUser());

        // 🔗 Forgot Password
        forgotPasswordTextView.setOnClickListener(v -> resetPassword());

        // 🔗 Sign up link
        signupTextView.setOnClickListener(v -> {
            startActivity(new Intent(Signin.this, Signup.class));
            finish();
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Minimum 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // ✅ Firebase Email + Password Sign-In
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Signin.this, MainPage.class));
                            finish();
                        }
                    } else {
                        String message = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Authentication failed.";
                        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Enter your email to reset password");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        String message = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Failed to send reset email.";
                        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
