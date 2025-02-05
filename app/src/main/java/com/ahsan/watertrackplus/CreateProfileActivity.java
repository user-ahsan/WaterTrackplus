package com.ahsan.watertrackplus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class CreateProfileActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "WaterTrackPrefs";
    private static final String KEY_PROFILE_CREATED = "profile_created";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_PICTURE = "profile_picture";
    
    private ShapeableImageView profileImage;
    private TextInputLayout usernameLayout;
    private TextInputEditText usernameInput;
    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private MaterialButton btnCreateProfile;
    private Uri currentPhotoUri;
    
    private final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    );

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    currentPhotoUri = imageUri;
                    profileImage.setImageURI(imageUri);
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profilePicture);
        usernameLayout = findViewById(R.id.tilName);
        usernameInput = findViewById(R.id.etName);
        emailLayout = findViewById(R.id.tilEmail);
        emailInput = findViewById(R.id.etEmail);
        btnCreateProfile = findViewById(R.id.btnCreateProfile);

        // Set initial profile image
        profileImage.setImageResource(R.drawable.ic_person);
    }

    private void setupListeners() {
        profileImage.setOnClickListener(v -> pickFromGallery());
        MaterialButton btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnChoosePhoto.setOnClickListener(v -> pickFromGallery());

        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsername(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCreateProfile.setOnClickListener(v -> validateAndSaveProfile());
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private boolean validateUsername(String username) {
        if (username.trim().isEmpty()) {
            usernameLayout.setError("Username is required");
            return false;
        }
        if (username.length() < 3) {
            usernameLayout.setError("Username must be at least 3 characters");
            return false;
        }
        usernameLayout.setError(null);
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.trim().isEmpty()) {
            emailLayout.setError("Email is required");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailLayout.setError("Invalid email address");
            return false;
        }
        emailLayout.setError(null);
        return true;
    }

    private void validateAndSaveProfile() {
        String username = usernameInput.getText().toString();
        String email = emailInput.getText().toString();

        if (!validateUsername(username) || !validateEmail(email)) {
            return;
        }

        // Save profile data
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        if (currentPhotoUri != null) {
            editor.putString(KEY_PROFILE_PICTURE, currentPhotoUri.toString());
        }
        editor.putBoolean(KEY_PROFILE_CREATED, true);
        editor.apply();

        // Start main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        
        // Use the new transition API
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 
                R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        finish();
    }
} 