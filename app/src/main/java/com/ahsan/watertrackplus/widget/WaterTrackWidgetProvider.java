package com.ahsan.watertrackplus.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.RemoteViews;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ahsan.watertrackplus.R;
import com.ahsan.watertrackplus.data.WaterDbHelper;

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
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        
        String action = intent.getAction();
        if (action != null) {
            if (action.equals("QUICK_ADD_WATER")) {
                handleQuickAdd(context, intent.getFloatExtra("amount", DEFAULT_QUICK_ADD));
            } else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
                // Force update all widgets
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName widgetComponent = new ComponentName(context, WaterTrackWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
                
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }
        }
    }

    private void handleQuickAdd(Context context, float amount) {
        try {
            // Get the database helper and preferences
            WaterDbHelper dbHelper = new WaterDbHelper(context);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            
            // Get the latest values from database and preferences
            float dailyGoal = prefs.getFloat("daily_goal", DEFAULT_DAILY_GOAL);
            float currentIntake = dbHelper.getTodayTotalIntake();
            
            // Check if adding this amount would exceed the daily goal
            if (currentIntake + amount > dailyGoal) {
                // Show warning notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "water_track")
                    .setSmallIcon(R.drawable.ic_water_drop)
                    .setContentTitle("Daily Goal Limit")
                    .setContentText(String.format("Cannot exceed daily goal of %.0f ml (Current: %.0f ml)", dailyGoal, currentIntake))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                if (ActivityCompat.checkSelfPermission(context, 
                    android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(2, builder.build());
                }
                return;
            }
            
            // Add water intake record
            long id = dbHelper.addWaterIntake((int)amount);
            
            if (id != -1) {
                // Get updated values after adding
                float totalIntake = dbHelper.getTodayTotalIntake();
                
                // Update widget preferences with latest values
                prefs.edit()
                    .putFloat("current_intake", totalIntake)
                    .putString("last_update_date", new java.text.SimpleDateFormat("yyyy-MM-dd")
                        .format(new java.util.Date()))
                    .apply();

                // Update all widgets
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName widgetComponent = new ComponentName(context, WaterTrackWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
                onUpdate(context, appWidgetManager, appWidgetIds);
                
                // Show success notification
                showQuickAddNotification(context, amount);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling quick add", e);
        }
    }

    private void showQuickAddNotification(Context context, float amount) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "water_track")
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentTitle("Water Intake Added")
                .setContentText(String.format("Added %.0f ml of water", amount))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, 
                android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(1, builder.build());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.water_track_widget);
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            
            // Check if it's a new day or first launch
            String savedDate = prefs.getString("last_update_date", "");
            String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(new java.util.Date());
            
            // If it's a new day or first launch, initialize the database
            if (!currentDate.equals(savedDate)) {
                WaterDbHelper dbHelper = new WaterDbHelper(context);
                float totalIntake = dbHelper.getTodayTotalIntake();
                
                prefs.edit()
                    .putFloat("current_intake", totalIntake)
                    .putString("last_update_date", currentDate)
                    .apply();
            }

            float dailyGoal = prefs.getFloat("daily_goal", DEFAULT_DAILY_GOAL);
            float currentIntake = prefs.getFloat("current_intake", 0);
            float quickAddAmount = prefs.getFloat(PREF_QUICK_ADD_AMOUNT, DEFAULT_QUICK_ADD);

            // Update progress views
            updateProgressViews(views, context, currentIntake, dailyGoal);
            
            // Setup quick add button with direct widget action
            Intent quickAddIntent = new Intent(context, WaterTrackWidgetProvider.class)
                .setAction("QUICK_ADD_WATER")
                .putExtra("amount", quickAddAmount);
            
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent quickAddPendingIntent = PendingIntent.getBroadcast(
                context, 
                appWidgetId,
                quickAddIntent, 
                flags
            );
            
            String buttonText = String.format("+ %.0f ml", quickAddAmount);
            views.setOnClickPendingIntent(R.id.widget_quick_add_button, quickAddPendingIntent);
            views.setTextViewText(R.id.widget_quick_add_button, buttonText);

            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating widget", e);
            showErrorState(views, context);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
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
                    ExistingPeriodicWorkPolicy.REPLACE,
                    updateWork
            );
            
            Log.d(TAG, "Widget updates scheduled");
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

    private void updateProgressViews(@NonNull RemoteViews views, @NonNull Context context,
                                   float currentIntake, float dailyGoal) {
        // Calculate progress percentage (0-100)
        int progress = (int)((currentIntake / dailyGoal) * 100);
        progress = Math.min(100, progress); // Cap at 100%
        
        // Update progress bar
        views.setProgressBar(R.id.widget_progress_bar, 100, progress, false);
        
        // Update text views
        String progressText = String.format(
            context.getString(R.string.widget_progress_format), 
            currentIntake/1000, 
            dailyGoal/1000
        );
        views.setTextViewText(R.id.widget_progress_text, progressText);
        
        float remaining = Math.max(0, dailyGoal - currentIntake);
        String remainingText = String.format(
            context.getString(R.string.widget_remaining_format), 
            remaining/1000
        );
        views.setTextViewText(R.id.widget_remaining_text, remainingText);
    }

    private void showErrorState(@NonNull RemoteViews views, @NonNull Context context) {
        views.setTextViewText(R.id.widget_progress_text, 
            context.getString(R.string.widget_error_update));
        views.setProgressBar(R.id.widget_progress_bar, 100, 0, false);
        views.setTextViewText(R.id.widget_remaining_text, 
            context.getString(R.string.widget_error_retry));
    }
} 