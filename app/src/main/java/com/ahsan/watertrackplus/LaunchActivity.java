package com.ahsan.watertrackplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.button.MaterialButton;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make navigation bar transparent
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        setContentView(R.layout.activity_launch);

        // Find views
        View placeholderImage = findViewById(R.id.ivPlaceholder);
        View cardContent = findViewById(R.id.cardContent);
        MaterialButton btnContinue = findViewById(R.id.btnContinue);

        // Set initial visibility
        placeholderImage.setAlpha(0f);
        cardContent.setAlpha(0f);
        placeholderImage.setTranslationY(50f);
        cardContent.setTranslationY(50f);

        // Load animation
        android.view.animation.Animation fadeSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up);
        fadeSlideUp.setDuration(800);

        // Start animations with different delays
        placeholderImage.postDelayed(() -> {
            placeholderImage.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .start();
        }, 100);
        
        cardContent.postDelayed(() -> {
            cardContent.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .start();
        }, 300);

        // Set click listener for Continue button
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(LaunchActivity.this, UserInfoActivity.class);
            startActivity(intent);
            finish(); // Close the launch activity
        });
    }
} 