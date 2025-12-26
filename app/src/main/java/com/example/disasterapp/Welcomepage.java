package com.example.disasterapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class Welcomepage extends AppCompatActivity {

    private MaterialButton btnLoginIn, btnSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcomepage);

        // Find buttons
        btnLoginIn = findViewById(R.id.btnLoginIn);
        btnSignIn = findViewById(R.id.btnSignIn);

        // "Join As Volunteer" → Sign Up Activity
        btnLoginIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcomepage.this, Signup.class);
                startActivity(intent);
            }
        });

        // "Sign In" → Sign In Activity
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcomepage.this, Signin.class);
                startActivity(intent);
            }
        });
    }
}
