package com.ahsan.watertrackplus.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ahsan.watertrackplus.R;
import com.ahsan.watertrackplus.ui.main.MainActivity;
import java.util.concurrent.TimeUnit;

public class WaterTrackWidgetProvider extends AppWidgetProvider {
    private static final String PREFS_NAME = "WaterTrackWidgetPrefs";
    private static final String PREF_QUICK_ADD_AMOUNT = "quick_add_amount";
    private static final String WIDGET_UPDATE_WORK = "water_track_widget_update_work";
    private static final int MIN_UPDATE_INTERVAL = 15; // minutes
    private static final float DEFAULT_DAILY_GOAL = 2500f; // ml
    private static final float DEFAULT_QUICK_ADD = 250f; // ml

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        try {
            scheduleWidgetUpdates(context);
            checkAndRequestPermissions(context);
        } catch (Exception e) {
            // Log error and handle gracefully
            e.printStackTrace();
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        try {
            // Clean up when last widget is removed
            WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try {
            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        try {
            // Update widget layout based on size changes
            updateWidget(context, appWidgetManager, appWidgetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.water_track_widget);
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            float dailyGoal = prefs.getFloat("daily_goal", DEFAULT_DAILY_GOAL);
            float currentIntake = prefs.getFloat("current_intake", 0);
            float quickAddAmount = prefs.getFloat(PREF_QUICK_ADD_AMOUNT, DEFAULT_QUICK_ADD);

            // Validate values
            dailyGoal = Math.max(dailyGoal, 500); // Minimum 500ml daily goal
            currentIntake = Math.max(0, Math.min(currentIntake, dailyGoal)); // Clamp between 0 and daily goal
            quickAddAmount = Math.max(50, Math.min(quickAddAmount, 1000)); // Clamp between 50ml and 1000ml

            updateProgressViews(views, context, currentIntake, dailyGoal);
            setupQuickAddButton(context, views, appWidgetId, quickAddAmount);

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            e.printStackTrace();
            // Show error state in widget
            showErrorState(context, views);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void updateProgressViews(@NonNull RemoteViews views, @NonNull Context context, float currentIntake, float dailyGoal) {
        String progressText = context.getString(R.string.widget_progress_format, currentIntake/1000, dailyGoal/1000);
        views.setTextViewText(R.id.widget_progress_text, progressText);
        
        int progress = (int)((currentIntake / dailyGoal) * 100);
        views.setProgressBar(R.id.widget_progress_bar, 100, progress, false);

        float remaining = Math.max(0, dailyGoal - currentIntake);
        String remainingText = context.getString(R.string.widget_remaining_format, remaining/1000);
        views.setTextViewText(R.id.widget_remaining_text, remainingText);

        // Update accessibility descriptions
        views.setContentDescription(R.id.widget_progress_text, 
            context.getString(R.string.widget_accessibility_progress, currentIntake/1000, dailyGoal/1000));
        views.setContentDescription(R.id.widget_remaining_text,
            context.getString(R.string.widget_accessibility_remaining, remaining/1000));
    }

    private void setupQuickAddButton(@NonNull Context context, @NonNull RemoteViews views, int appWidgetId, float quickAddAmount) {
        Intent quickAddIntent = new Intent(context, MainActivity.class)
                .setAction("QUICK_ADD_WATER")
                .putExtra("amount", quickAddAmount)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent quickAddPendingIntent = PendingIntent.getActivity(context, appWidgetId,
                quickAddIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        views.setOnClickPendingIntent(R.id.widget_quick_add_button, quickAddPendingIntent);
        views.setTextViewText(R.id.widget_quick_add_button, 
            context.getString(R.string.widget_quick_add_format, quickAddAmount));
    }

    private void showErrorState(@NonNull Context context, @NonNull RemoteViews views) {
        views.setTextViewText(R.id.widget_progress_text, context.getString(R.string.widget_error_update));
        views.setProgressBar(R.id.widget_progress_bar, 100, 0, false);
        views.setTextViewText(R.id.widget_remaining_text, context.getString(R.string.widget_error_retry));
    }

    private void scheduleWidgetUpdates(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        PeriodicWorkRequest updateWork = new PeriodicWorkRequest.Builder(
                WidgetUpdateWorker.class, MIN_UPDATE_INTERVAL, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInitialDelay(MIN_UPDATE_INTERVAL, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WIDGET_UPDATE_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateWork);
    }

    private void checkAndRequestPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                ActivityCompat.requestPermissions(
                        (MainActivity) context,
                        new String[]{
                                android.Manifest.permission.SCHEDULE_EXACT_ALARM,
                                android.Manifest.permission.USE_EXACT_ALARM
                        },
                        100);
            } catch (Exception e) {
                e.printStackTrace();
                // Handle permission request failure
                ActivityCompat.requestPermissions(
                        (MainActivity) context,
                        new String[]{
                                android.Manifest.permission.SCHEDULE_EXACT_ALARM,
                                android.Manifest.permission.USE_EXACT_ALARM
                        },
                        100);
            }
        }
    }
} 