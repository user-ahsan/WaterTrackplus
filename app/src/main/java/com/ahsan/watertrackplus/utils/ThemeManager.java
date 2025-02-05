package com.ahsan.watertrackplus.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.ahsan.watertrackplus.R;
import com.google.android.material.color.MaterialColors;

/**
 * Manages theme-related operations across the app
 */
public class ThemeManager {
    private static ThemeManager instance;
    private final PreferencesManager preferencesManager;
    
    private static final int THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    private static final int THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES;
    private static final int THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    private ThemeManager(Context context) {
        preferencesManager = PreferencesManager.getInstance(context);
    }

    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context);
        }
        return instance;
    }

    /**
     * Initialize theme settings for the activity
     */
    public void initializeTheme(@NonNull Activity activity) {
        int savedTheme = preferencesManager.getThemeMode(THEME_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
        
        if (activity instanceof AppCompatActivity) {
            setupWindowDecorations((AppCompatActivity) activity);
        }
    }

    /**
     * Set up window decorations based on theme
     */
    private void setupWindowDecorations(@NonNull AppCompatActivity activity) {
        Window window = activity.getWindow();
        if (window == null) return;

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // Get the window controller
        View decorView = window.getDecorView();
        WindowInsetsControllerCompat insetsController = 
            WindowCompat.getInsetsController(window, decorView);

        // Determine if we're in dark mode
        boolean isDarkMode = (activity.getResources().getConfiguration().uiMode 
            & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        // Set status bar appearance
        insetsController.setAppearanceLightStatusBars(!isDarkMode);
        insetsController.setAppearanceLightNavigationBars(!isDarkMode);

        // Set navigation bar color
        window.setNavigationBarColor(
            MaterialColors.getColor(activity, android.R.attr.colorBackground, 0)
        );
        
        // Set status bar color
        window.setStatusBarColor(
            MaterialColors.getColor(activity, android.R.attr.colorBackground, 0)
        );

        // Handle scrim on API 29+ for transparent nav bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }
    }

    /**
     * Update theme mode
     */
    public void setThemeMode(@NonNull Activity activity, int themeMode) {
        if (themeMode != THEME_LIGHT && themeMode != THEME_DARK && themeMode != THEME_SYSTEM) {
            themeMode = THEME_SYSTEM;
        }
        
        preferencesManager.setThemeMode(themeMode);
        AppCompatDelegate.setDefaultNightMode(themeMode);
        
        // Recreate the activity to apply theme changes
        activity.recreate();
    }

    /**
     * Get current theme mode
     */
    public int getCurrentThemeMode() {
        return preferencesManager.getThemeMode(THEME_SYSTEM);
    }

    /**
     * Check if dark mode is active
     */
    public boolean isDarkMode(@NonNull Context context) {
        return (context.getResources().getConfiguration().uiMode 
            & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Apply theme to a specific activity without recreation
     */
    public void applyThemeToActivity(@NonNull AppCompatActivity activity) {
        setupWindowDecorations(activity);
    }

    /**
     * Update status and navigation bar colors for specific activities that need custom colors
     */
    public void setCustomBarColors(@NonNull Activity activity, int statusBarColor, 
                                 int navigationBarColor, boolean lightStatusBar, 
                                 boolean lightNavigationBar) {
        Window window = activity.getWindow();
        if (window == null) return;

        WindowInsetsControllerCompat insetsController = 
            WindowCompat.getInsetsController(window, window.getDecorView());

        window.setStatusBarColor(statusBarColor);
        window.setNavigationBarColor(navigationBarColor);
        
        insetsController.setAppearanceLightStatusBars(lightStatusBar);
        insetsController.setAppearanceLightNavigationBars(lightNavigationBar);
    }
} 