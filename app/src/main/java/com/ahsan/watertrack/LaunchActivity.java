package com.ahsan.watertrack;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import com.ahsan.watertrack.databinding.ActivityLaunchBinding;

public class LaunchActivity extends AppCompatActivity {
    private ActivityLaunchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLaunchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Start animations after a short delay
        binding.getRoot().post(() -> startAnimations());

        binding.btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            // Add slide up exit transition
            overridePendingTransition(R.anim.slide_up_enter, R.anim.slide_up_exit);
        });
    }

    private void startAnimations() {
        // Create fade in animations for each view
        ObjectAnimator imageViewFade = createFadeAnimation(binding.ivPlaceholder);
        ObjectAnimator titleFade = createFadeAnimation(binding.tvTitle);
        ObjectAnimator buttonFade = createFadeAnimation(binding.btnContinue);

        // Create slide up animations
        ObjectAnimator titleSlide = createSlideAnimation(binding.tvTitle);
        ObjectAnimator buttonSlide = createSlideAnimation(binding.btnContinue);

        // Combine animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(1000);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        
        // Play animations in sequence
        animatorSet.play(imageViewFade).before(titleFade);
        animatorSet.play(titleFade).with(titleSlide);
        animatorSet.play(buttonFade).with(buttonSlide).after(titleFade);
        
        animatorSet.start();
    }

    private ObjectAnimator createFadeAnimation(View view) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);
        return fadeIn;
    }

    private ObjectAnimator createSlideAnimation(View view) {
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(view, "translationY", 100f, 0f);
        slideUp.setDuration(1000);
        return slideUp;
    }
} 