package com.ahsan.watertrackplus;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.ahsan.watertrackplus.base.BaseFragment;
import com.ahsan.watertrackplus.widget.WidgetUpdateHelper;
import com.ahsan.watertrackplus.utils.MaterialToast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;

public class MeFragment extends BaseFragment {
    private View rootView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences widgetPrefs;
    private static final String WIDGET_PREFS_NAME = "widget_preferences";
    private static final String KEY_QUICK_ADD_AMOUNT = "quick_add_amount";
    private static final String KEY_LAST_UPDATE_DATE = "last_update_date";
    private static final float DEFAULT_QUICK_ADD = 250f;
    private static final String PREF_THEME_MODE = "theme_mode";
    private static final String PREF_PROFILE_PICTURE = "profile_picture_uri";
    private RadioGroup themeRadioGroup;
    private Uri currentPhotoUri;
    private ShapeableImageView profilePicture;
    private Dialog currentDialog;
    private View dialogView;
    private boolean isFragmentActive = false;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null && isFragmentActive) {
                updateProfilePicture(uri);
            }
        }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
            widgetPrefs = getContext().getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_me, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isFragmentActive = true;

        setupClickListeners();
        loadUserProfile();
        setupWidgetSettings();
        setupThemeSettings();
    }

    private void setupClickListeners() {
        MaterialButton btnEditProfile = rootView.findViewById(R.id.btnEditProfile);
        SwitchMaterial switchNotifications = rootView.findViewById(R.id.switchNotifications);
        profilePicture = rootView.findViewById(R.id.ivProfilePicture);
        
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> 
            toggleNotifications(isChecked));
    }

    private void showEditProfileDialog() {
        if (!isFragmentActive || getContext() == null) return;

        try {
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }

            dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null);

            setupDialogViews();
            setupDialogButtons();
            showDialog();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error showing dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDialogViews() {
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        ShapeableImageView ivEditProfilePicture = dialogView.findViewById(R.id.ivEditProfilePicture);

        String currentName = sharedPreferences.getString("user_name", "");
        String currentEmail = sharedPreferences.getString("user_email", "");
        etName.setText(currentName);
        etEmail.setText(currentEmail);

        String savedProfilePicture = sharedPreferences.getString(PREF_PROFILE_PICTURE, null);
        if (savedProfilePicture != null) {
            ivEditProfilePicture.setImageURI(Uri.parse(savedProfilePicture));
        } else {
            ivEditProfilePicture.setImageResource(R.drawable.ic_person);
        }
    }

    private void setupDialogButtons() {
        MaterialButton btnChoosePhoto = dialogView.findViewById(R.id.btnChoosePhoto);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnChoosePhoto.setOnClickListener(v -> chooseFromGallery());
        btnSave.setOnClickListener(v -> validateAndSave());
        btnCancel.setOnClickListener(v -> currentDialog.dismiss());
    }

    private void showDialog() {
        currentDialog = new Dialog(requireContext());
        currentDialog.setContentView(dialogView);
        
        if (currentDialog.getWindow() != null) {
            currentDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        currentDialog.show();
    }

    private void validateAndSave() {
        TextInputLayout tilName = dialogView.findViewById(R.id.tilName);
        TextInputLayout tilEmail = dialogView.findViewById(R.id.tilEmail);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        
        boolean isValid = true;
        
        if (name.isEmpty()) {
            tilName.setError("Name cannot be empty");
            isValid = false;
        } else {
            tilName.setError(null);
        }
        
        if (!isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }
        
        if (isValid) {
            updateUserProfile(name, email);
            currentDialog.dismiss();
        }
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void chooseFromGallery() {
        pickImageLauncher.launch("image/*");
    }

    private void updateProfilePicture(Uri uri) {
        if (!isFragmentActive || uri == null) return;

        try {
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                requireContext().getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            }

            if (dialogView != null) {
                ShapeableImageView ivEditProfilePicture = dialogView.findViewById(R.id.ivEditProfilePicture);
                ivEditProfilePicture.setImageURI(uri);
            }

            profilePicture.setImageURI(uri);
            
            sharedPreferences.edit()
                .putString(PREF_PROFILE_PICTURE, uri.toString())
                .apply();

            Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error updating profile picture", Toast.LENGTH_SHORT).show();
            profilePicture.setImageResource(R.drawable.ic_person);
        }
    }

    private void loadUserProfile() {
        String userName = sharedPreferences.getString("user_name", "User Name");
        String userEmail = sharedPreferences.getString("user_email", "user@example.com");
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false);

        TextView tvUserName = rootView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = rootView.findViewById(R.id.tvUserEmail);
        SwitchMaterial switchNotifications = rootView.findViewById(R.id.switchNotifications);

        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);
        switchNotifications.setChecked(notificationsEnabled);

        String savedProfilePicture = sharedPreferences.getString(PREF_PROFILE_PICTURE, null);
        if (savedProfilePicture != null && profilePicture != null) {
            profilePicture.setImageURI(Uri.parse(savedProfilePicture));
        } else if (profilePicture != null) {
            profilePicture.setImageResource(R.drawable.ic_person);
        }
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
        // Get the quick add amount from widget preferences
        float quickAddAmount = widgetPrefs.getFloat(KEY_QUICK_ADD_AMOUNT, DEFAULT_QUICK_ADD);
        
        Slider quickAddSlider = rootView.findViewById(R.id.quick_add_amount_slider);
        TextView quickAddText = rootView.findViewById(R.id.quick_add_amount_text);
        MaterialButton saveButton = rootView.findViewById(R.id.btnSaveWidgetSettings);

        if (quickAddSlider != null) {
            // Set initial value
            quickAddSlider.setValue(quickAddAmount);
            
            // Update text when slider changes
            quickAddSlider.addOnChangeListener((slider, value, fromUser) -> {
                quickAddText.setText(String.format(java.util.Locale.US, 
                    "Quick add amount: %.0fml", value));
            });
        }

        // Set initial text
        quickAddText.setText(String.format(java.util.Locale.US, 
            "Quick add amount: %.0fml", quickAddAmount));

        // Save button click handler
        saveButton.setOnClickListener(v -> saveWidgetSettings());
    }

    private void saveWidgetSettings() {
        Slider quickAddSlider = rootView.findViewById(R.id.quick_add_amount_slider);
        if (quickAddSlider != null) {
            float quickAddAmount = quickAddSlider.getValue();
            
            // Save settings to shared preferences
            widgetPrefs.edit()
                .putFloat(KEY_QUICK_ADD_AMOUNT, quickAddAmount)
                .putString(KEY_LAST_UPDATE_DATE, new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(new java.util.Date()))
                .apply();

            // Update both widgets using the helper
            WidgetUpdateHelper.updateAllWidgets(requireContext());

            // Show success message
            Toast.makeText(requireContext(), "Quick add amount updated to " + (int)quickAddAmount + "ml", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupThemeSettings() {
        themeRadioGroup = rootView.findViewById(R.id.themeRadioGroup);
        if (themeRadioGroup == null) return;

        int currentTheme = sharedPreferences.getInt(PREF_THEME_MODE, 
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                themeRadioGroup.check(R.id.radioLight);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                themeRadioGroup.check(R.id.radioDark);
                break;
            default:
                themeRadioGroup.check(R.id.radioSystem);
                break;
        }

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int themeMode;
            if (checkedId == R.id.radioLight) {
                themeMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.radioDark) {
                themeMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            
            sharedPreferences.edit().putInt(PREF_THEME_MODE, themeMode).apply();
            AppCompatDelegate.setDefaultNightMode(themeMode);
            
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).recreateAfterThemeChange(R.id.navigation_me);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentActive = false;
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        currentDialog = null;
        dialogView = null;
    }
} 