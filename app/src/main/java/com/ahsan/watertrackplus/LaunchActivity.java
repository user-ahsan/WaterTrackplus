package com.ahsan.watertrackplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import android.widget.ImageView;
import android.widget.TextView;

public class LaunchActivity extends AppCompatActivity {

    private ImageView ivLogo;
    private TextView tvAppName;

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

        // Initialize views
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);

        // Set initial visibility and position
        ivLogo.setAlpha(0f);
        tvAppName.setAlpha(0f);
        ivLogo.setTranslationY(50f);
        tvAppName.setTranslationY(50f);

        // Start animations with different delays
        ivLogo.postDelayed(() -> {
            ivLogo.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .start();
        }, 100);
        
        tvAppName.postDelayed(() -> {
            tvAppName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .start();
        }, 300);

        // Auto-navigate to MainActivity after delay
        ivLogo.postDelayed(() -> {
            Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000); // 2 seconds delay
    }
} 