package com.example.firebasetesting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Splash screen duration (in milliseconds)
    private static final int SPLASH_TIME_OUT = 3000;  // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);  // Set the splash screen layout

        // Use a Handler to delay the transition to the main activity
        new Handler().postDelayed(() -> {
            // After the splash time, move to the main activity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);  // Start LoginActivity
            startActivity(intent);
            finish();  // Close SplashActivity so it's not in the back stack
        }, SPLASH_TIME_OUT);
    }
}
