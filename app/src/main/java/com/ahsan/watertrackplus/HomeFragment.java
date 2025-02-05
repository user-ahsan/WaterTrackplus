package com.ahsan.watertrackplus;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ahsan.watertrackplus.base.BaseFragment;
import com.ahsan.watertrackplus.data.WaterDbHelper;
import com.ahsan.watertrackplus.utils.MaterialToast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import android.content.SharedPreferences;
import android.content.Context;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import com.ahsan.watertrackplus.widget.WaterTrackWidgetProvider;
import com.ahsan.watertrackplus.utils.WaterIntakeManager;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ahsan.watertrackplus.widget.WidgetUpdateHelper;

public class HomeFragment extends BaseFragment {
    
    private static final String TAG = "HomeFragment";
    private static final String WIDGET_PREFS_NAME = "widget_preferences";
    private static final String KEY_DAILY_GOAL = "daily_goal";
    private static final String KEY_CURRENT_INTAKE = "current_intake";
    private static final String KEY_LAST_UPDATE_DATE = "last_update_date";
    private static final float DEFAULT_DAILY_GOAL = 2500f; // ml

    private View rootView;
    private TextView tvHealthScore;
    private TextView tvWaterIntakeStatus;
    private CircularProgressIndicator circularProgress;
    private TextInputEditText etDailyGoal;
    private MaterialButton btnUpdateGoal;
    private ChipGroup chipGroup;
    private Dialog customDialog;
    private WaterDbHelper dbHelper;
    private SharedPreferences widgetPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize database helper
        dbHelper = new WaterDbHelper(requireContext());
        
        // Initialize widget preferences
        widgetPrefs = requireContext().getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE);
        
        // Initialize views
        tvHealthScore = view.findViewById(R.id.tvHealthScore);
        tvWaterIntakeStatus = view.findViewById(R.id.tvWaterIntakeStatus);
        circularProgress = view.findViewById(R.id.circularProgress);
        etDailyGoal = view.findViewById(R.id.etDailyGoal);
        btnUpdateGoal = view.findViewById(R.id.btnUpdateGoal);
        
        setupChips(view);
        setupDailyGoal();
        checkDateChange();

        // Load data
        loadData();
    }

    private void setupChips(View view) {
        Chip chip150ml = view.findViewById(R.id.chip150ml);
        Chip chip250ml = view.findViewById(R.id.chip250ml);
        Chip chip500ml = view.findViewById(R.id.chip500ml);
        Chip chipCustom = view.findViewById(R.id.chipCustom);

        // Set click listeners
        chip150ml.setOnClickListener(v -> handleAmountSelection(150));
        chip250ml.setOnClickListener(v -> handleAmountSelection(250));
        chip500ml.setOnClickListener(v -> handleAmountSelection(500));
        chipCustom.setOnClickListener(v -> showCustomAmountDialog());
    }

    private void handleAmountSelection(int amount) {
        // Get daily goal and current intake
        float dailyGoal = widgetPrefs.getFloat(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);
        float currentIntake = dbHelper.getTodayTotalIntake();
        
        // Calculate remaining amount to reach goal
        float remainingToGoal = dailyGoal - currentIntake;
        
        if (remainingToGoal <= 0) {
            return;
        }

        // If adding full amount would exceed goal, only add what's needed
        int amountToAdd = amount;
        if (currentIntake + amount > dailyGoal) {
            amountToAdd = (int) remainingToGoal;
        }

        // Add water intake record to database
        long id = dbHelper.addWaterIntake(amountToAdd);
        
        if (id != -1) {
            // Update UI
            updateProgress();
            // Update widget
            updateWidget();
        }
    }

    private void showCustomAmountDialog() {
        if (getContext() == null) return;

        customDialog = new Dialog(getContext());
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.dialog_custom_amount);
        
        if (customDialog.getWindow() != null) {
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            customDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Initialize dialog views
        TextInputEditText etAmount = customDialog.findViewById(R.id.etAmount);
        ChipGroup chipGroupSuggestions = customDialog.findViewById(R.id.chipGroupSuggestions);
        MaterialButton btnCancel = customDialog.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = customDialog.findViewById(R.id.btnConfirm);

        // Set click listeners
        btnCancel.setOnClickListener(v -> {
            clearInputFocus(etAmount);
            customDialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                try {
                    int amount = Integer.parseInt(amountStr);
                    if (amount > 0 && amount <= 2000) {
                        clearInputFocus(etAmount);
                        handleAmountSelection(amount);
                        customDialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    // Invalid input, just ignore
                }
            }
        });

        // Handle chip suggestions
        chipGroupSuggestions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                if (checkedId == R.id.chip400) {
                    etAmount.setText("400");
                    clearInputFocus(etAmount);
                } else if (checkedId == R.id.chip750) {
                    etAmount.setText("750");
                    clearInputFocus(etAmount);
                } else if (checkedId == R.id.chip1000) {
                    etAmount.setText("1000");
                    clearInputFocus(etAmount);
                }
            }
        });

        // Set dialog dismiss listener to clear focus
        customDialog.setOnDismissListener(dialog -> clearInputFocus(etAmount));

        customDialog.show();
    }

    private void clearInputFocus(TextInputEditText editText) {
        editText.clearFocus();
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void setupDailyGoal() {
        float currentGoal = widgetPrefs.getFloat(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);
        etDailyGoal.setText(String.valueOf((int)currentGoal));

        // Clear focus when done editing
        etDailyGoal.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                clearInputFocus(etDailyGoal);
            }
        });

        btnUpdateGoal.setOnClickListener(v -> handleGoalUpdate());

        // Handle keyboard done action
        etDailyGoal.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                btnUpdateGoal.performClick();
                return true;
            }
            return false;
        });
    }

    private void handleGoalUpdate() {
        String goalStr = etDailyGoal.getText().toString();
        if (!goalStr.isEmpty()) {
            try {
                float newGoal = Float.parseFloat(goalStr);
                float oldGoal = widgetPrefs.getFloat(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);
                
                if (newGoal >= 500 && newGoal <= 5000) {
                    float currentIntake = dbHelper.getTodayTotalIntake();

                    if (currentIntake > 0) {
                        if (newGoal < oldGoal && currentIntake > newGoal) {
                            // Attempting to decrease goal below current intake
                            new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Warning: Exceeded Intake")
                                .setMessage(String.format("Your current intake (%.0f ml) exceeds the new goal (%.0f ml). What would you like to do?", currentIntake, newGoal))
                                .setPositiveButton("Keep Current Goal", (dialog, which) -> {
                                    // Revert the input field to old goal
                                    etDailyGoal.setText(String.valueOf((int)oldGoal));
                                })
                                .setNegativeButton("Delete Excess Records", (dialog, which) -> {
                                    // Show confirmation dialog for deleting records
                                    new MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Confirm Delete")
                                        .setMessage("This will delete all records for today and set the new goal. Are you sure?")
                                        .setPositiveButton("Yes", (innerDialog, innerWhich) -> {
                                            dbHelper.clearAllRecords();
                                            updateGoalAndWidget(newGoal, false);
                                        })
                                        .setNegativeButton("No", null)
                                        .show();
                                })
                                .show();
                        } else {
                            // Normal goal update with existing records
                            new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Keep Today's Records?")
                                .setMessage("Do you want to keep today's water intake records?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    if (currentIntake > newGoal) {
                                        // Show warning that goal is already met
                                        new MaterialAlertDialogBuilder(requireContext())
                                            .setTitle("Goal Already Met")
                                            .setMessage(String.format("Your current intake (%.0f ml) already exceeds the new goal (%.0f ml). The goal will be updated but you won't be able to add more water today.", currentIntake, newGoal))
                                            .setPositiveButton("OK", (innerDialog, innerWhich) -> {
                                                updateGoalAndWidget(newGoal, true);
                                            })
                                            .show();
                                    } else {
                                        updateGoalAndWidget(newGoal, true);
                                    }
                                })
                                .setNegativeButton("No", (dialog, which) -> {
                                    // Clear today's records and update goal
                                    dbHelper.clearAllRecords();
                                    updateGoalAndWidget(newGoal, false);
                                })
                                .show();
                        }
                    } else {
                        // No records exist, simply update the goal
                        updateGoalAndWidget(newGoal, true);
                    }
                } else {
                    MaterialToast.showWarning(requireContext(), 
                        "Please enter a goal between 500ml and 5000ml");
                }
            } catch (NumberFormatException e) {
                MaterialToast.showError(requireContext(), "Please enter a valid number");
            }
        }
    }

    private void updateGoalAndWidget(float newGoal, boolean keepRecords) {
        widgetPrefs.edit()
            .putFloat(KEY_DAILY_GOAL, newGoal)
            .putFloat(KEY_CURRENT_INTAKE, keepRecords ? dbHelper.getTodayTotalIntake() : 0)
            .putString(KEY_LAST_UPDATE_DATE, new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date()))
            .apply();

        // Update UI
        updateProgress();
        clearInputFocus(etDailyGoal);

        // Update widget
        WidgetUpdateHelper.updateAllWidgets(requireContext());
    }

    private void checkDateChange() {
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd")
            .format(new java.util.Date());
        String lastUpdateDate = widgetPrefs.getString(KEY_LAST_UPDATE_DATE, "");
        
        if (!currentDate.equals(lastUpdateDate)) {
            // Reset widget progress for new day
            updateWidget();
            widgetPrefs.edit()
                .putString(KEY_LAST_UPDATE_DATE, currentDate)
                .apply();
        }
    }

    private void loadData() {
        updateProgress();
    }

    private void updateProgress() {
        // Get daily goal from preferences
        float dailyGoal = widgetPrefs.getFloat(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);
        
        // Get today's total intake
        float totalIntake = dbHelper.getTodayTotalIntake();
        
        // Update UI with animation
        WaterIntakeManager.updateWaterIntakeUI(
            requireContext(),
            circularProgress,
            tvHealthScore,
            tvWaterIntakeStatus,
            totalIntake,
            dailyGoal
        );
        
        // Save current intake and date to widget preferences
        widgetPrefs.edit()
            .putFloat(KEY_CURRENT_INTAKE, totalIntake)
            .putFloat(KEY_DAILY_GOAL, dailyGoal)
            .putString(KEY_LAST_UPDATE_DATE, new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(new java.util.Date()))
            .apply();
        
        // Update widget
        updateWidget();
    }

    private void updateWidget() {
        // Update both widgets using the helper
        WidgetUpdateHelper.updateAllWidgets(requireContext());
    }

    @Override
    protected boolean handleBackPress() {
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkDateChange();
        updateProgress();
    }
} 