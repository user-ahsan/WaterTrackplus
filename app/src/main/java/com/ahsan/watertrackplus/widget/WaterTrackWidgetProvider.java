package com.ahsan.watertrackplus.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.RemoteViews;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ahsan.watertrackplus.R;
import com.ahsan.watertrackplus.MainActivity;

import java.util.concurrent.TimeUnit;

public class WaterTrackWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "WaterTrackWidget";
    public static final String PREFS_NAME = "WaterTrackWidgetPrefs";
    public static final String PREF_QUICK_ADD_AMOUNT = "quick_add_amount";
    public static final String WIDGET_UPDATE_WORK = "water_track_widget_update_work";
    private static final int MIN_UPDATE_INTERVAL = 15; // minutes
    private static final float DEFAULT_DAILY_GOAL = 2500f; // ml
    private static final float DEFAULT_QUICK_ADD = 250f; // ml

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "Widget enabled");
        try {
            scheduleWidgetUpdates(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkAlarmPermissions(context);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onEnabled", e);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(TAG, "Widget disabled");
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK);
        } catch (Exception e) {
            Log.e(TAG, "Error in onDisabled", e);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "Updating widget");
        try {
            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onUpdate", e);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d(TAG, "Widget options changed");
        try {
            updateWidget(context, appWidgetManager, appWidgetId);
        } catch (Exception e) {
            Log.e(TAG, "Error in onAppWidgetOptionsChanged", e);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.water_track_widget);
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            float dailyGoal = prefs.getFloat("daily_goal", DEFAULT_DAILY_GOAL);
            float currentIntake = prefs.getFloat("current_intake", 0);
            float quickAddAmount = prefs.getFloat(PREF_QUICK_ADD_AMOUNT, DEFAULT_QUICK_ADD);

            dailyGoal = Math.max(dailyGoal, 500);
            currentIntake = Math.max(0, Math.min(currentIntake, dailyGoal));
            quickAddAmount = Math.max(50, Math.min(quickAddAmount, 1000));

            updateProgressViews(views, context, currentIntake, dailyGoal);
            setupQuickAddButton(context, views, appWidgetId, quickAddAmount);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.d(TAG, String.format("Widget updated - Progress: %.1f/%.1f ml", currentIntake, dailyGoal));
        } catch (Exception e) {
            Log.e(TAG, "Error updating widget", e);
            showErrorState(context, views);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void updateProgressViews(@NonNull RemoteViews views, @NonNull Context context, 
                                   float currentIntake, float dailyGoal) {
        String progressText = String.format(
            context.getString(R.string.widget_progress_format), 
            currentIntake/1000, 
            dailyGoal/1000
        );
        views.setTextViewText(R.id.widget_progress_text, progressText);
        
        int progress = (int)((currentIntake / dailyGoal) * 100);
        views.setProgressBar(R.id.widget_progress_bar, 100, progress, false);

        float remaining = Math.max(0, dailyGoal - currentIntake);
        String remainingText = String.format(
            context.getString(R.string.widget_remaining_format), 
            remaining/1000
        );
        views.setTextViewText(R.id.widget_remaining_text, remainingText);

        String accessibilityProgress = String.format(
            context.getString(R.string.widget_accessibility_progress), 
            currentIntake/1000, 
            dailyGoal/1000
        );
        String accessibilityRemaining = String.format(
            context.getString(R.string.widget_accessibility_remaining), 
            remaining/1000
        );
        
        views.setContentDescription(R.id.widget_progress_text, accessibilityProgress);
        views.setContentDescription(R.id.widget_remaining_text, accessibilityRemaining);
    }

    private void setupQuickAddButton(@NonNull Context context, @NonNull RemoteViews views, 
                                   int appWidgetId, float quickAddAmount) {
        Intent quickAddIntent = new Intent(context, MainActivity.class)
                .setAction("QUICK_ADD_WATER")
                .putExtra("amount", quickAddAmount)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent quickAddPendingIntent = PendingIntent.getActivity(
            context, 
            appWidgetId,
            quickAddIntent, 
            flags
        );
        
        String buttonText = String.format(
            context.getString(R.string.widget_quick_add_format), 
            quickAddAmount
        );
        views.setOnClickPendingIntent(R.id.widget_quick_add_button, quickAddPendingIntent);
        views.setTextViewText(R.id.widget_quick_add_button, buttonText);
        views.setContentDescription(
            R.id.widget_quick_add_button, 
            context.getString(R.string.widget_accessibility_quick_add)
        );
    }

    private void showErrorState(@NonNull Context context, @NonNull RemoteViews views) {
        views.setTextViewText(
            R.id.widget_progress_text, 
            context.getString(R.string.widget_error_update)
        );
        views.setProgressBar(R.id.widget_progress_bar, 100, 0, false);
        views.setTextViewText(
            R.id.widget_remaining_text, 
            context.getString(R.string.widget_error_retry)
        );
    }

    private void scheduleWidgetUpdates(Context context) {
        try {
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
            
            Log.d(TAG, "Widget updates scheduled successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling widget updates", e);
        }
    }

    private void checkAlarmPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.i(TAG, "Requesting exact alarm permission");
            }
        }
    }
} 