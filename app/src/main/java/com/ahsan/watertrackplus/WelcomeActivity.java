package com.ahsan.watertrackplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";
    private static final String PREFS_NAME = "WaterTrackPrefs";
    private static final String KEY_FIRST_TIME = "first_time";
    private static final String KEY_PROFILE_CREATED = "profile_created";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Check if it's first time launch
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isFirstTime = prefs.getBoolean(KEY_FIRST_TIME, true);
            boolean isProfileCreated = prefs.getBoolean(KEY_PROFILE_CREATED, false);
            
            if (!isFirstTime && isProfileCreated) {
                startMainActivity();
                return;
            }

            setContentView(R.layout.activity_welcome);

            // Initialize views
            ShapeableImageView welcomeLogo = findViewById(R.id.welcomeLogo);
            View welcomeTitle = findViewById(R.id.welcomeTitle);
            View welcomeMessage = findViewById(R.id.welcomeMessage);
            MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);

            // Load animations
            Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

            // Apply animations with delays
            welcomeLogo.startAnimation(fadeIn);
            
            welcomeTitle.setVisibility(View.INVISIBLE);
            welcomeMessage.setVisibility(View.INVISIBLE);
            btnGetStarted.setVisibility(View.INVISIBLE);

            welcomeTitle.postDelayed(() -> {
                welcomeTitle.setVisibility(View.VISIBLE);
                welcomeTitle.startAnimation(slideInRight);
            }, 500);

            welcomeMessage.postDelayed(() -> {
                welcomeMessage.setVisibility(View.VISIBLE);
                welcomeMessage.startAnimation(slideInRight);
            }, 700);

            btnGetStarted.postDelayed(() -> {
                btnGetStarted.setVisibility(View.VISIBLE);
                btnGetStarted.startAnimation(fadeIn);
            }, 900);

            // Set up click listener
            btnGetStarted.setOnClickListener(v -> {
                // Mark first time as false but keep profile_created as false until profile is created
                prefs.edit()
                    .putBoolean(KEY_FIRST_TIME, false)
                    .apply();
                
                // Start create profile activity
                Intent intent = new Intent(WelcomeActivity.this, CreateProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                
                // Use the new transition API
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 
                        R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                finish();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            startMainActivity();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
} 