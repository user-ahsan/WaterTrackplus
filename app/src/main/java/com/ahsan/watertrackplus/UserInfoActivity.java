package com.ahsan.watertrackplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.NumberPicker;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class UserInfoActivity extends AppCompatActivity {

    private TextInputEditText etName, etNickname;
    private NumberPicker agePicker;
    private MaterialButton btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make navigation bar transparent
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        setContentView(R.layout.activity_user_info);

        // Initialize views
        etName = findViewById(R.id.etName);
        etNickname = findViewById(R.id.etNickname);
        agePicker = findViewById(R.id.agePicker);
        btnContinue = findViewById(R.id.btnContinue);

        // Setup age picker
        setupAgePicker();

        // Setup continue button
        btnContinue.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserInfo();
                navigateToMainActivity();
            }
        });
    }

    private void setupAgePicker() {
        agePicker.setMinValue(12);
        agePicker.setMaxValue(100);
        agePicker.setValue(25);
        agePicker.setWrapSelectorWheel(false);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Please enter your name");
            isValid = false;
        }

        String nickname = etNickname.getText().toString().trim();
        if (nickname.isEmpty()) {
            etNickname.setError("Please enter a nickname");
            isValid = false;
        }

        return isValid;
    }

    private void saveUserInfo() {
        // TODO: Save user information to SharedPreferences or database
        String name = etName.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        int age = agePicker.getValue();
        boolean isMale = findViewById(R.id.rbMale).isSelected();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
} 