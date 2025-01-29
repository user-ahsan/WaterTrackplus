package com.ahsan.watertrackplus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ahsan.watertrackplus.base.BaseFragment;

public class ProfileFragment extends BaseFragment {

    private TextView tvName;
    private TextView tvEmail;
    private View layoutEditProfile;
    private View layoutNotifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        layoutEditProfile = view.findViewById(R.id.layoutEditProfile);
        layoutNotifications = view.findViewById(R.id.layoutNotifications);

        // Setup click listeners
        setupClickListeners();
        
        // Load user data
        loadUserData();
    }

    private void setupClickListeners() {
        layoutEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to edit profile screen
            showToast("Edit Profile clicked");
        });

        layoutNotifications.setOnClickListener(v -> {
            // TODO: Navigate to notifications settings
            showToast("Notifications clicked");
        });
    }

    private void loadUserData() {
        // TODO: Load actual user data from preferences/database
        tvName.setText("John Doe");
        tvEmail.setText("john.doe@example.com");
    }

    @Override
    protected boolean handleBackPress() {
        return false; // Let activity handle back press
    }
} 