package com.ahsan.watertrackplus;

import android.os.Bundle;
import android.view.WindowManager;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private long lastBackPressTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();
        setupBackNavigation();

        if (savedInstanceState == null) {
            // Load HomeFragment as the default fragment
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.navigation_discover) {
                loadFragment(new DiscoverFragment());
                return true;
            } else if (itemId == R.id.navigation_me) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}