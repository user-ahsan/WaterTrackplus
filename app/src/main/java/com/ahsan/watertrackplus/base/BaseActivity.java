package com.ahsan.watertrackplus.base;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ahsan.watertrackplus.utils.DialogManager;
import com.ahsan.watertrackplus.utils.PreferencesManager;
import com.ahsan.watertrackplus.utils.ThemeManager;

/**
 * Base activity class that handles common functionality across all activities
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected final String TAG = getClass().getSimpleName();
    protected DialogManager dialogManager;
    protected PreferencesManager preferencesManager;
    protected ThemeManager themeManager;
    protected boolean isDestroyed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            // Initialize managers before super.onCreate
            initializeManagers();
            
            // Apply theme before super.onCreate
            if (themeManager != null) {
                themeManager.initializeTheme(this);
            } else {
                Log.e(TAG, "ThemeManager is null during initialization");
            }
            
            super.onCreate(savedInstanceState);
            
            // Initialize views and data after super.onCreate and setContentView
            onInitialize(savedInstanceState);
        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            handleCriticalError(e);
        }
    }

    protected void initializeManagers() {
        try {
            dialogManager = new DialogManager(this);
            preferencesManager = PreferencesManager.getInstance(this);
            themeManager = ThemeManager.getInstance(this);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing managers", e);
            handleError(e);
        }
    }

    /**
     * Called after super.onCreate to initialize views and data
     */
    protected void onInitialize(@Nullable Bundle savedInstanceState) {
        try {
            initializeViews();
            setupListeners();
            initializeData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onInitialize", e);
            handleError(e);
        }
    }

    /**
     * Initialize views for the activity
     */
    protected abstract void initializeViews();

    /**
     * Setup click listeners and other view interactions
     */
    protected abstract void setupListeners();

    /**
     * Initialize any data needed for the activity
     */
    protected abstract void initializeData();

    /**
     * Handle non-critical errors
     */
    protected void handleError(Exception e) {
        Log.e(TAG, "Error occurred", e);
        try {
            showToast("An error occurred: " + e.getMessage());
        } catch (Exception toastError) {
            Log.e(TAG, "Error showing error toast", toastError);
        }
    }

    /**
     * Handle critical errors that prevent the activity from functioning
     */
    protected void handleCriticalError(Exception e) {
        Log.e(TAG, "Critical error occurred", e);
        try {
            if (dialogManager != null) {
                dialogManager.showErrorDialog(
                    "Critical Error",
                    "A critical error occurred: " + e.getMessage()
                );
            } else {
                showToast("Critical error: " + e.getMessage());
            }
            finish();
        } catch (Exception finalError) {
            Log.e(TAG, "Error handling critical error", finalError);
            finish();
        }
    }

    /**
     * Show a toast message safely
     */
    protected void showToast(String message) {
        try {
            if (!isDestroyed) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast", e);
        }
    }

    /**
     * Show loading state safely
     */
    protected void showLoading() {
        try {
            if (!isDestroyed && dialogManager != null) {
                dialogManager.showLoadingDialog();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing loading dialog", e);
        }
    }

    /**
     * Hide loading state safely
     */
    protected void hideLoading() {
        try {
            if (dialogManager != null) {
                dialogManager.dismissLoadingDialog();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding loading dialog", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (themeManager != null) {
                themeManager.applyThemeToActivity(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme in onResume", e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            isDestroyed = true;
            if (dialogManager != null) {
                dialogManager.cleanup();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        } finally {
            super.onDestroy();
        }
    }
} 