package com.ahsan.watertrackplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private long lastBackPressTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000; // 2 seconds
    private static final String PREF_THEME_MODE = "theme_mode";
    private static final String KEY_SELECTED_TAB = "selected_tab";
    private int selectedTabId = R.id.navigation_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // Apply theme before super.onCreate and setContentView
            applyTheme();
            
            super.onCreate(savedInstanceState);
            
            // Enable edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            
            setContentView(R.layout.activity_main);

            // Restore selected tab
            if (savedInstanceState != null) {
                selectedTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.navigation_home);
            }

            bottomNavigationView = findViewById(R.id.bottomNavigation);
            if (bottomNavigationView == null) {
                Log.e(TAG, "Bottom navigation view not found");
                finish();
                return;
            }

            setupBottomNavigation();
            setupBackNavigation();

            if (savedInstanceState == null) {
                // Load initial fragment based on selectedTabId
                loadFragmentForId(selectedTabId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bottomNavigationView != null) {
            outState.putInt(KEY_SELECTED_TAB, bottomNavigationView.getSelectedItemId());
        }
    }

    private void applyTheme() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE);
            int themeMode = sharedPreferences.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            AppCompatDelegate.setDefaultNightMode(themeMode);
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme", e);
        }
    }

    private void setupBottomNavigation() {
        try {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                selectedTabId = itemId;
                loadFragmentForId(itemId);
                return true;
            });

            // Set saved selection
            bottomNavigationView.setSelectedItemId(selectedTabId);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation", e);
        }
    }

    private void loadFragmentForId(int itemId) {
        try {
            Fragment fragment = null;
            String fragmentTag = "";

            if (itemId == R.id.navigation_home) {
                fragment = new HomeFragment();
                fragmentTag = "HomeFragment";
            } else if (itemId == R.id.navigation_me) {
                fragment = new MeFragment();
                fragmentTag = "MeFragment";
            }

            if (fragment == null) {
                Log.e(TAG, "Failed to create fragment for id: " + itemId);
                return;
            }

            // Check if the fragment is already loaded
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (currentFragment != null && currentFragment.isVisible()) {
                return;
            }

            loadFragment(fragment, fragmentTag);
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment for id: " + itemId, e);
        }
    }

    private void setupBackNavigation() {
        try {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentContainer);

                    // If we're not on the home tab
                    if (bottomNavigationView.getSelectedItemId() != R.id.navigation_home) {
                        // Switch to home tab
                        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                        return;
                    }

                    // If we're on home tab, handle double back press to exit
                    if (System.currentTimeMillis() - lastBackPressTime < BACK_PRESS_INTERVAL) {
                        setEnabled(false);
                        finishAffinity();
                    } else {
                        lastBackPressTime = System.currentTimeMillis();
                        showToast("Press back again to exit");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up back navigation", e);
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            
            // Add animations
            transaction.setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            );
            
            transaction.replace(R.id.fragmentContainer, fragment, tag);
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment: " + tag, e);
        }
    }

    private void showToast(String message) {
        try {
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast", e);
        }
    }

    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    public void recreateAfterThemeChange(int targetTab) {
        try {
            selectedTabId = targetTab;
            
            // Add a short delay to allow the theme to be applied
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing()) {
                    getWindow().setWindowAnimations(R.style.WindowAnimationFade);
                    recreate();
                }
            }, 100);
        } catch (Exception e) {
            Log.e(TAG, "Error recreating activity after theme change", e);
        }
    }
}