package com.ahsan.watertrackplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

public class MeFragment extends Fragment {

    private ShapeableImageView ivProfilePicture;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private MaterialButton btnEditProfile;
    private SwitchMaterial switchNotifications;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // Initialize UI components
        initializeViews(view);
        setupClickListeners();
        loadUserProfile();
    }

    private void initializeViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        switchNotifications = view.findViewById(R.id.switchNotifications);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> toggleNotifications(isChecked));
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);

        // Pre-fill current values
        etName.setText(tvUserName.getText());
        etEmail.setText(tvUserEmail.getText());

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                if (!name.isEmpty() && !email.isEmpty()) {
                    updateUserProfile(name, email);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void loadUserProfile() {
        String userName = sharedPreferences.getString("user_name", "User Name");
        String userEmail = sharedPreferences.getString("user_email", "user@example.com");

        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);

        // Load notification state
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void toggleNotifications(boolean enable) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications_enabled", enable);
        editor.apply();

        String status = enable ? "enabled" : "disabled";
        Toast.makeText(requireContext(), "Notifications " + status, Toast.LENGTH_SHORT).show();
    }

    public void updateUserProfile(String name, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", name);
        editor.putString("user_email", email);
        editor.apply();

        tvUserName.setText(name);
        tvUserEmail.setText(email);
        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
    }
} 