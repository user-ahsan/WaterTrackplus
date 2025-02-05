package com.ahsan.watertrackplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahsan.watertrackplus.base.BaseActivity;
import com.ahsan.watertrackplus.data.WaterDbHelper;
import com.ahsan.watertrackplus.BuildConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LaunchActivity extends BaseActivity {
    private static final String TAG = "LaunchActivity";
    private ImageView ivLogo;
    private TextView tvAppName;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_launch);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeViews() {
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);

        // Set initial visibility and position
        ivLogo.setAlpha(0f);
        tvAppName.setAlpha(0f);
        ivLogo.setTranslationY(50f);
        tvAppName.setTranslationY(50f);
    }

    @Override
    protected void setupListeners() {
        // Start animations with different delays
        handler.postDelayed(() -> {
            if (!isDestroyed) {
                ivLogo.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .start();
            }
        }, 100);
        
        handler.postDelayed(() -> {
            if (!isDestroyed) {
                tvAppName.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .start();
            }
        }, 300);

        // Check app state and navigate after delay
        handler.postDelayed(() -> {
            if (!isDestroyed) {
                navigateToAppropriateScreen();
            }
        }, 2000); // 2 seconds delay
    }

    @Override
    protected void initializeData() {
        // No initial data needed
    }

    private void navigateToAppropriateScreen() {
        final Context appContext = getApplicationContext();
        executor.execute(() -> {
            WaterDbHelper dbHelper = null;
            try {
                // Get current app state
                boolean isFirstTime = preferencesManager.isFirstTime();
                boolean isProfileCreated = preferencesManager.isProfileCreated();

                // Log initial state for debugging
                Log.d(TAG, String.format("App State - Debug: %b, FirstTime: %b, ProfileCreated: %b",
                    BuildConfig.DEBUG, isFirstTime, isProfileCreated));

                // Initialize database check
                dbHelper = new WaterDbHelper(appContext);
                boolean hasData = dbHelper.hasAnyWaterIntakeRecords();
                Log.d(TAG, "Database has data: " + hasData);

                final Intent intent;
                
                if (BuildConfig.DEBUG) {
                    // Debug mode: Always show welcome screen for testing
                    Log.d(TAG, "Debug build: Forcing welcome screen");
                    intent = new Intent(appContext, WelcomeActivity.class);
                } else {
                    // Release mode: Show welcome screen only on first launch
                    if (isFirstTime || !isProfileCreated) {
                        Log.i(TAG, "Release build: First launch detected");
                        // Only update first time flag in release mode
                        preferencesManager.setFirstTime(false);
                        intent = new Intent(appContext, WelcomeActivity.class);
                    } else if (!hasData) {
                        Log.i(TAG, "Release build: No data found, resetting state");
                        // Reset app state if no data exists
                        preferencesManager.setFirstTime(true);
                        preferencesManager.setProfileCreated(false);
                        intent = new Intent(appContext, WelcomeActivity.class);
                    } else {
                        Log.i(TAG, "Release build: Normal launch to main screen");
                        intent = new Intent(appContext, MainActivity.class);
                    }
                }

                // Add necessary flags for proper navigation
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // Post navigation on main thread
                handler.post(() -> {
                    if (!isDestroyed) {
                        try {
                            Log.d(TAG, "Starting activity: " + intent.getComponent().getClassName());
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "Error starting activity", e);
                            handleCriticalError(e);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error in navigateToAppropriateScreen", e);
                handler.post(() -> {
                    if (!isDestroyed) {
                        handleCriticalError(e);
                    }
                });
            } finally {
                // Ensure database is always closed
                if (dbHelper != null) {
                    try {
                        dbHelper.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing database", e);
                    }
                }
            }
        });
    }

    @Override
    protected void handleCriticalError(Exception e) {
        super.handleCriticalError(e);
        navigateToWelcomeScreen();
    }

    private void navigateToWelcomeScreen() {
        try {
            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to welcome screen", e);
        } finally {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            executor.shutdown();
        } catch (Exception e) {
            Log.e(TAG, "Error shutting down executor", e);
        }
        super.onDestroy();
    }
} 