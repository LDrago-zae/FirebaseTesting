package com.example.firebasetesting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page); // Ensure this is the correct layout

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if the user is already logged in
        if (mAuth.getCurrentUser() != null) {
            // If user is logged in, go to HomeActivity directly
            goToHomeActivity();
        }

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener for Login button
        buttonLogin.setOnClickListener(v -> loginUser());

        // Set click listener for "Already have an account?" button
        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(v -> goToSignUpActivity());

        Button googleLogin = findViewById(R.id.googleLogin);
        googleLogin.setOnClickListener(v -> {
            // Handle Google login here
            Toast.makeText(LoginActivity.this, "Google login clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Log in user
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                // Login success
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                goToHomeActivity();
            } else {
                // If login fails, display a message
                Toast.makeText(LoginActivity.this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }

    private void goToSignUpActivity() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }
}
