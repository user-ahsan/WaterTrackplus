package com.ahsan.watertrackplus;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ahsan.watertrackplus.widget.WaterTrackWidgetProvider;

import java.io.File;
import java.io.IOException;

public class MeFragment extends Fragment {
    private View rootView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences widgetPrefs;
    private static final float DEFAULT_QUICK_ADD = 250f;
    private static final String PREF_THEME_MODE = "theme_mode";
    private static final String PREF_PROFILE_PICTURE = "profile_picture_uri";
    private RadioGroup themeRadioGroup;
    private Uri currentPhotoUri;
    private ShapeableImageView profilePicture;
    private MaterialAlertDialogBuilder dialogBuilder;
    private View dialogView;
    private Dialog currentDialog;
    private boolean isFragmentActive = false;

    // Activity Result Launchers
    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && isFragmentActive) {
                updateProfilePicture(currentPhotoUri);
            }
        }
    );

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
        // Initialize SharedPreferences early
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);
            widgetPrefs = getContext().getSharedPreferences(WaterTrackWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_me, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isFragmentActive = true;

        // Initialize core functionality first
        setupClickListeners();
        loadUserProfile();

        // Initialize optional features with error handling
        try {
        setupWidgetSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
        setupThemeSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void setupClickListeners() {
        if (rootView == null) return;

        MaterialButton btnEditProfile = rootView.findViewById(R.id.btnEditProfile);
        SwitchMaterial switchNotifications = rootView.findViewById(R.id.switchNotifications);
        profilePicture = rootView.findViewById(R.id.ivProfilePicture);
        
        if (btnEditProfile != null) {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }
        
        if (switchNotifications != null) {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> toggleNotifications(isChecked));
        }
    }

    private void showEditProfileDialog() {
        if (!isFragmentActive || getContext() == null) return;

        try {
            // Dismiss any existing dialog
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }

            dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
            if (dialogView == null) return;

            TextInputLayout tilName = dialogView.findViewById(R.id.tilName);
            TextInputLayout tilEmail = dialogView.findViewById(R.id.tilEmail);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
            ShapeableImageView ivEditProfilePicture = dialogView.findViewById(R.id.ivEditProfilePicture);
            MaterialButton btnTakePhoto = dialogView.findViewById(R.id.btnTakePhoto);
            MaterialButton btnChoosePhoto = dialogView.findViewById(R.id.btnChoosePhoto);
            MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Pre-fill current values
        TextView tvUserName = rootView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = rootView.findViewById(R.id.tvUserEmail);
            if (tvUserName != null && tvUserEmail != null && etName != null && etEmail != null) {
        etName.setText(tvUserName.getText());
        etEmail.setText(tvUserEmail.getText());
            }

            // Load current profile picture
            if (ivEditProfilePicture != null) {
                String savedProfilePicture = sharedPreferences.getString(PREF_PROFILE_PICTURE, null);
                if (savedProfilePicture != null) {
                    try {
                        Uri savedUri = Uri.parse(savedProfilePicture);
                        ivEditProfilePicture.setImageURI(savedUri);
                        if (ivEditProfilePicture.getDrawable() == null) {
                            ivEditProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ivEditProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                }
            }

            // Setup photo buttons
            if (btnTakePhoto != null) {
                btnTakePhoto.setOnClickListener(v -> takePhoto());
            }
            if (btnChoosePhoto != null) {
                btnChoosePhoto.setOnClickListener(v -> chooseFromGallery());
            }

            // Create dialog using MaterialAlertDialogBuilder for consistent styling
            currentDialog = new Dialog(requireContext());
            currentDialog.setContentView(dialogView);
            
            // Set dialog window attributes
            if (currentDialog.getWindow() != null) {
                currentDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                currentDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            // Setup save button click listener
            if (btnSave != null) {
                btnSave.setOnClickListener(v -> {
                    if (!isFragmentActive) return;
                    
                    String name = etName != null ? etName.getText().toString().trim() : "";
                    String email = etEmail != null ? etEmail.getText().toString().trim() : "";
                    
                    boolean isValid = true;
                    
                    if (name.isEmpty()) {
                        if (tilName != null) tilName.setError("Name cannot be empty");
                        isValid = false;
                    } else {
                        if (tilName != null) tilName.setError(null);
                    }
                    
                    if (!isValidEmail(email)) {
                        if (tilEmail != null) tilEmail.setError("Please enter a valid email address");
                        isValid = false;
                    } else {
                        if (tilEmail != null) tilEmail.setError(null);
                    }
                    
                    if (isValid) {
                    updateUserProfile(name, email);
                        if (currentDialog != null && currentDialog.isShowing()) {
                            currentDialog.dismiss();
                        }
                    }
                });
            }

            // Setup cancel button click listener
            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> {
                    if (currentDialog != null && currentDialog.isShowing()) {
                        currentDialog.dismiss();
                    }
                });
            }

            currentDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error showing dialog", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(requireContext(),
                    "com.ahsan.watertrackplus.fileprovider",
                    photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                takePictureLauncher.launch(takePictureIntent);
            }
        }
    }

    private void chooseFromGallery() {
        pickImageLauncher.launch("image/*");
    }

    private File createImageFile() throws IOException {
        String imageFileName = "PROFILE_PICTURE";
        File storageDir = requireContext().getFilesDir();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void updateProfilePicture(Uri uri) {
        if (!isFragmentActive || uri == null || getContext() == null) return;

        try {
            // For gallery images, try to take persistable permission
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                try {
                    getContext().getContentResolver().takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException e) {
                    // If we can't get persistable permission, we can still try to load the image
                    e.printStackTrace();
                }
            }

            // Update dialog image if dialog is showing
            if (dialogView != null) {
                ShapeableImageView ivEditProfilePicture = dialogView.findViewById(R.id.ivEditProfilePicture);
                if (ivEditProfilePicture != null) {
                    ivEditProfilePicture.setImageURI(null); // Clear the previous image
                    ivEditProfilePicture.setImageURI(uri);
                    if (ivEditProfilePicture.getDrawable() == null) {
                        ivEditProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                        throw new Exception("Failed to load image in dialog");
                    }
                }
            }

            // Update main profile image
            if (profilePicture != null) {
                profilePicture.setImageURI(null); // Clear the previous image
                profilePicture.setImageURI(uri);
                if (profilePicture.getDrawable() == null) {
                    profilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                    throw new Exception("Failed to load image in profile");
                }
            }
            
            // Save the URI to SharedPreferences only if image loading was successful
            if (sharedPreferences != null) {
                sharedPreferences.edit()
                    .putString(PREF_PROFILE_PICTURE, uri.toString())
                    .apply();
            }

            if (getContext() != null) {
                Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error updating profile picture", Toast.LENGTH_SHORT).show();
            }
            // Reset to default image
            if (profilePicture != null) {
                profilePicture.setImageResource(R.drawable.ic_launcher_foreground);
            }
            if (dialogView != null) {
                ShapeableImageView ivEditProfilePicture = dialogView.findViewById(R.id.ivEditProfilePicture);
                if (ivEditProfilePicture != null) {
                    ivEditProfilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                }
            }
            // Clear invalid URI from preferences
            if (sharedPreferences != null) {
                sharedPreferences.edit().remove(PREF_PROFILE_PICTURE).apply();
            }
        }
    }

    private void loadUserProfile() {
        if (rootView == null || !isFragmentActive) return;

        try {
        String userName = sharedPreferences.getString("user_name", "User Name");
        String userEmail = sharedPreferences.getString("user_email", "user@example.com");

        TextView tvUserName = rootView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = rootView.findViewById(R.id.tvUserEmail);
            profilePicture = rootView.findViewById(R.id.ivProfilePicture);
        SwitchMaterial switchNotifications = rootView.findViewById(R.id.switchNotifications);

            if (tvUserName != null) tvUserName.setText(userName);
            if (tvUserEmail != null) tvUserEmail.setText(userEmail);

            // Load profile picture if exists
            if (profilePicture != null) {
                String savedProfilePicture = sharedPreferences.getString(PREF_PROFILE_PICTURE, null);
                if (savedProfilePicture != null) {
                    try {
                        Uri savedUri = Uri.parse(savedProfilePicture);
                        profilePicture.setImageURI(savedUri);
                        
                        // If image loading fails, reset to default
                        if (profilePicture.getDrawable() == null) {
                            profilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                            // Clear invalid URI from preferences
                            sharedPreferences.edit().remove(PREF_PROFILE_PICTURE).apply();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        profilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                }
            }

        // Load notification state
            if (switchNotifications != null) {
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false);
        switchNotifications.setChecked(notificationsEnabled);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
            }
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
        if (rootView == null || !isFragmentActive) return;

        try {
        float quickAddAmount = widgetPrefs.getFloat(WaterTrackWidgetProvider.PREF_QUICK_ADD_AMOUNT, DEFAULT_QUICK_ADD);
        
        // Set initial values
        Slider quickAddSlider = rootView.findViewById(R.id.quick_add_amount_slider);
        TextView quickAddText = rootView.findViewById(R.id.quick_add_amount_text);
        MaterialButton saveButton = rootView.findViewById(R.id.btnSaveWidgetSettings);

            if (quickAddSlider != null) {
        quickAddSlider.setValue(quickAddAmount);
        quickAddSlider.addOnChangeListener((slider, value, fromUser) -> {
            updateQuickAddText(value);
        });
            }

            if (quickAddText != null) {
                updateQuickAddText(quickAddAmount);
            }

            if (saveButton != null) {
        saveButton.setOnClickListener(v -> saveWidgetSettings());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void setupThemeSettings() {
        if (rootView == null || !isFragmentActive) return;

        try {
        themeRadioGroup = rootView.findViewById(R.id.themeRadioGroup);
        RadioButton radioLight = rootView.findViewById(R.id.radioLight);
        RadioButton radioDark = rootView.findViewById(R.id.radioDark);
        RadioButton radioSystem = rootView.findViewById(R.id.radioSystem);

        // Load saved theme setting
        int currentTheme = sharedPreferences.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        // Set the correct radio button
        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                radioLight.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                radioDark.setChecked(true);
                break;
            default:
                radioSystem.setChecked(true);
                break;
        }

        // Handle theme changes
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int themeMode;
            if (checkedId == R.id.radioLight) {
                themeMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.radioDark) {
                themeMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            
            // Save the theme preference
            sharedPreferences.edit().putInt(PREF_THEME_MODE, themeMode).apply();
            
                // Apply the theme
            AppCompatDelegate.setDefaultNightMode(themeMode);
                
                // Recreate activity with proper navigation
                if (getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.recreateAfterThemeChange(R.id.navigation_me);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error setting up theme options", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 