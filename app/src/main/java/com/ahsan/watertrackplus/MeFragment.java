package com.ahsan.watertrackplus;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.ahsan.watertrackplus.widget.WaterTrackWidgetProvider;

public class MeFragment extends Fragment {
    private View rootView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences widgetPrefs;
    private static final float DEFAULT_QUICK_ADD = 250f;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_me, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        widgetPrefs = requireContext().getSharedPreferences(WaterTrackWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE);

        setupClickListeners();
        loadUserProfile();
        setupWidgetSettings();
    }

    private void setupClickListeners() {
        MaterialButton btnEditProfile = rootView.findViewById(R.id.btnEditProfile);
        SwitchMaterial switchNotifications = rootView.findViewById(R.id.switchNotifications);
        
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> toggleNotifications(isChecked));
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);

        // Pre-fill current values
        TextView tvUserName = rootView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = rootView.findViewById(R.id.tvUserEmail);
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

        TextView tvUserName = rootView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = rootView.findViewById(R.id.tvUserEmail);
        SwitchMaterial switchNotifications = rootView.findViewById(R.id.switchNotifications);

        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);

        // Load notification state
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void toggleNotifications(boolean enable) {
        sharedPreferences.edit()
            .putBoolean("notifications_enabled", enable)
            .apply();

        String status = enable ? "enabled" : "disabled";
        Toast.makeText(requireContext(), "Notifications " + status, Toast.LENGTH_SHORT).show();
    }

    private void updateUserProfile(String name, String email) {
        sharedPreferences.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .apply();

        TextView tvUserName = rootView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = rootView.findViewById(R.id.tvUserEmail);
        tvUserName.setText(name);
        tvUserEmail.setText(email);
        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
    }

    private void setupWidgetSettings() {
        float quickAddAmount = widgetPrefs.getFloat(WaterTrackWidgetProvider.PREF_QUICK_ADD_AMOUNT, DEFAULT_QUICK_ADD);
        
        // Set initial values
        Slider quickAddSlider = rootView.findViewById(R.id.quick_add_amount_slider);
        TextView quickAddText = rootView.findViewById(R.id.quick_add_amount_text);
        MaterialButton saveButton = rootView.findViewById(R.id.btnSaveWidgetSettings);

        quickAddSlider.setValue(quickAddAmount);
        updateQuickAddText(quickAddAmount);

        // Setup slider listener
        quickAddSlider.addOnChangeListener((slider, value, fromUser) -> {
            updateQuickAddText(value);
        });

        // Setup save button
        saveButton.setOnClickListener(v -> saveWidgetSettings());
    }

    private void updateQuickAddText(float value) {
        TextView quickAddText = rootView.findViewById(R.id.quick_add_amount_text);
        quickAddText.setText(String.format(java.util.Locale.US, "Quick add amount: %.0fml", value));
    }

    private void saveWidgetSettings() {
        Slider quickAddSlider = rootView.findViewById(R.id.quick_add_amount_slider);
        float quickAddAmount = quickAddSlider.getValue();
        
        // Save to preferences
        widgetPrefs.edit()
            .putFloat(WaterTrackWidgetProvider.PREF_QUICK_ADD_AMOUNT, quickAddAmount)
            .apply();

        // Force update all widgets
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(requireContext());
        ComponentName widgetComponent = new ComponentName(requireContext(), WaterTrackWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
        
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            // Trigger an explicit update
            Intent updateIntent = new Intent(requireContext(), WaterTrackWidgetProvider.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            requireContext().sendBroadcast(updateIntent);
            
            // Also directly update the widgets
            WaterTrackWidgetProvider widgetProvider = new WaterTrackWidgetProvider();
            widgetProvider.onUpdate(requireContext(), appWidgetManager, appWidgetIds);
        }
    }
} 