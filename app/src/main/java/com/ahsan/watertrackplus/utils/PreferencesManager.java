package com.ahsan.watertrackplus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Centralized manager for SharedPreferences operations
 */
public class PreferencesManager {
    private static final String PREFS_NAME = "WaterTrackPrefs";
    private static final String WIDGET_PREFS_NAME = "widget_preferences";
    
    // User Profile Keys
    private static final String KEY_FIRST_TIME = "first_time";
    private static final String KEY_PROFILE_CREATED = "profile_created";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_PICTURE = "profile_picture";
    
    // Settings Keys
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_QUICK_ADD_AMOUNT = "quick_add_amount";
    
    private static PreferencesManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences widgetPrefs;

    private PreferencesManager(Context context) {
        prefs = context.getApplicationContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        widgetPrefs = context.getApplicationContext()
            .getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    // User Profile Methods
    public boolean isFirstTime() {
        return prefs.getBoolean(KEY_FIRST_TIME, true);
    }

    public void setFirstTime(boolean isFirstTime) {
        prefs.edit().putBoolean(KEY_FIRST_TIME, isFirstTime).apply();
    }

    public boolean isProfileCreated() {
        return prefs.getBoolean(KEY_PROFILE_CREATED, false);
    }

    public void setProfileCreated(boolean created) {
        prefs.edit().putBoolean(KEY_PROFILE_CREATED, created).apply();
    }

    public void saveUserProfile(@NonNull String username, @NonNull String email, @Nullable Uri profilePicture) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        if (profilePicture != null) {
            editor.putString(KEY_PROFILE_PICTURE, profilePicture.toString());
        }
        editor.putBoolean(KEY_PROFILE_CREATED, true);
        editor.apply();
    }

    @Nullable
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    @Nullable
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    @Nullable
    public Uri getProfilePicture() {
        String uriString = prefs.getString(KEY_PROFILE_PICTURE, null);
        return uriString != null ? Uri.parse(uriString) : null;
    }

    // Settings Methods
    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public int getThemeMode(int defaultMode) {
        return prefs.getInt(KEY_THEME_MODE, defaultMode);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false);
    }

    // Widget Methods
    public void setQuickAddAmount(float amount) {
        widgetPrefs.edit().putFloat(KEY_QUICK_ADD_AMOUNT, amount).apply();
    }

    public float getQuickAddAmount(float defaultAmount) {
        return widgetPrefs.getFloat(KEY_QUICK_ADD_AMOUNT, defaultAmount);
    }

    // Clear Methods
    public void clearUserProfile() {
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_EMAIL)
            .remove(KEY_PROFILE_PICTURE)
            .remove(KEY_PROFILE_CREATED)
            .apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
        widgetPrefs.edit().clear().apply();
    }
} 