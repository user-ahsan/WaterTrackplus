package com.ahsan.watertrackplus.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import com.ahsan.watertrackplus.data.WaterDbHelper;

public class WaterDropletWidgetConfigureActivity extends Activity {
    private static final String WIDGET_PREFS_NAME = "widget_preferences";
    private static final String KEY_DAILY_GOAL = "daily_goal";
    private static final String KEY_CURRENT_INTAKE = "current_intake";
    private static final String KEY_LAST_UPDATE_DATE = "last_update_date";
    private static final String KEY_QUICK_ADD_AMOUNT = "quick_add_amount";
    private static final float DEFAULT_DAILY_GOAL = 2500f;
    private static final float DEFAULT_QUICK_ADD = 250f;
    
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // Configure and return success
        configureWidget();
    }

    private void configureWidget() {
        // Initialize preferences if needed
        SharedPreferences prefs = getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE);
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(new java.util.Date());
            
        // Get current intake from database
        try (WaterDbHelper dbHelper = new WaterDbHelper(this)) {
            float currentIntake = dbHelper.getTodayTotalIntake();
            
            // Initialize or update preferences
            prefs.edit()
                .putFloat(KEY_DAILY_GOAL, prefs.getFloat(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL))
                .putFloat(KEY_CURRENT_INTAKE, currentIntake)
                .putFloat(KEY_QUICK_ADD_AMOUNT, prefs.getFloat(KEY_QUICK_ADD_AMOUNT, DEFAULT_QUICK_ADD))
                .putString(KEY_LAST_UPDATE_DATE, currentDate)
                .apply();
        }

        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        WaterDropletWidgetProvider.updateWidget(this, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
} 