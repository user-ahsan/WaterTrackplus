package com.ahsan.watertrackplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private long lastBackPressTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000; // 2 seconds
    private static final String PREF_THEME_MODE = "theme_mode";
    private static final String KEY_SELECTED_TAB = "selected_tab";
    private int selectedTabId = R.id.navigation_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        setupBottomNavigation();
        setupBackNavigation();

        if (savedInstanceState == null) {
            // Load initial fragment based on selectedTabId
            loadFragmentForId(selectedTabId);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, bottomNavigationView.getSelectedItemId());
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        int themeMode = sharedPreferences.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            selectedTabId = itemId;
            loadFragmentForId(itemId);
            return true;
        });

        // Set saved selection
        bottomNavigationView.setSelectedItemId(selectedTabId);
    }

    private void loadFragmentForId(int itemId) {
        Fragment fragment;
        if (itemId == R.id.navigation_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.navigation_discover) {
            fragment = new DiscoverFragment();
        } else if (itemId == R.id.navigation_me) {
            fragment = new MeFragment();
        } else {
            fragment = new HomeFragment();
        }
        loadFragment(fragment);
    }

    private void setupBackNavigation() {
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
    }

    private void loadFragment(Fragment fragment) {
        // Don't load the same fragment again
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        // Add animations
        transaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
        );
        
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    public void recreateAfterThemeChange(int targetTab) {
        selectedTabId = targetTab;
        
        // Add a short delay to allow the theme to be applied
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                getWindow().setWindowAnimations(R.style.WindowAnimationFade);
                recreate();
            }
        }, 100);
    }
}