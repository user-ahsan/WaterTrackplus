package com.ahsan.watertrackplus.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.ahsan.watertrackplus.R;

public class WaterIntakeManager {
    private static final int ANIMATION_DURATION = 1000;
    private static final float LOW_THRESHOLD = 0.3f;
    private static final float MEDIUM_THRESHOLD = 0.7f;

    public static void updateWaterIntakeUI(Context context, 
                                         CircularProgressIndicator progressIndicator,
                                         TextView scoreText,
                                         TextView statusText,
                                         float currentIntake,
                                         float dailyGoal) {
        
        float percentage = Math.min(currentIntake / dailyGoal, 1f) * 100;
        
        // Animate the progress
        ValueAnimator animator = ValueAnimator.ofFloat(progressIndicator.getProgress(), percentage);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            progressIndicator.setProgress(Math.round(animatedValue));
            scoreText.setText(String.format("%.0f", animatedValue));
            
            // Update colors based on progress
            int color;
            String status;
            
            if (animatedValue < LOW_THRESHOLD * 100) {
                color = ContextCompat.getColor(context, R.color.progress_low);
                status = "Low water intake! Stay hydrated for better health.";
            } else if (animatedValue < MEDIUM_THRESHOLD * 100) {
                color = ContextCompat.getColor(context, R.color.progress_medium);
                status = "Getting there! Keep drinking water regularly.";
            } else {
                color = ContextCompat.getColor(context, R.color.progress_high);
                status = "Great job! You're well hydrated!";
            }
            
            progressIndicator.setIndicatorColor(color);
            scoreText.setTextColor(color);
            statusText.setText(status);
        });
        
        animator.start();
    }
} 