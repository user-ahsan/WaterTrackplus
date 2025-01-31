package com.ahsan.watertrackplus.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.util.Locale;

import com.ahsan.watertrackplus.MainActivity;
import com.ahsan.watertrackplus.R;
import com.ahsan.watertrackplus.data.WaterDbHelper;

public class WaterDropletWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "WaterDropletWidget";
    private static final float DEFAULT_DAILY_GOAL = 2500f;
    private static final String ACTION_QUICK_ADD = "com.ahsan.watertrackplus.QUICK_ADD";
    private static final String ACTION_VIEW_STATS = "com.ahsan.watertrackplus.VIEW_STATS";
    private static final int ANIMATION_DURATION = 1000;
    public static final String PREFS_NAME = "WaterTrackWidgetPrefs";
    public static final String PREF_QUICK_ADD_AMOUNT = "quick_add_amount";

    private BroadcastReceiver dataUpdateReceiver;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        registerDataUpdateReceiver(context);
        updateAllWidgets(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        unregisterDataUpdateReceiver(context);
    }

    private void registerDataUpdateReceiver(Context context) {
        if (dataUpdateReceiver == null) {
            dataUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (WidgetUpdateHelper.ACTION_DATA_UPDATED.equals(intent.getAction())) {
                        updateAllWidgets(context);
                    }
                }
            };
            context.getApplicationContext().registerReceiver(
                dataUpdateReceiver, 
                new IntentFilter(WidgetUpdateHelper.ACTION_DATA_UPDATED)
            );
        }
    }

    private void unregisterDataUpdateReceiver(Context context) {
        if (dataUpdateReceiver != null) {
            try {
                context.getApplicationContext().unregisterReceiver(dataUpdateReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
            dataUpdateReceiver = null;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateDropletWidget(context, appWidgetManager, appWidgetId, true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        
        String action = intent.getAction();
        if (ACTION_QUICK_ADD.equals(action)) {
            handleQuickAdd(context);
        } else if (ACTION_VIEW_STATS.equals(action)) {
            launchMainActivity(context);
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
            }
        }
    }

    private void updateDropletWidget(Context context, AppWidgetManager appWidgetManager, 
                                   int appWidgetId, boolean animate) {
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.water_droplet_widget);
            
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            float dailyGoal = prefs.getFloat("daily_goal", DEFAULT_DAILY_GOAL);
            float currentIntake = prefs.getFloat("current_intake", 0f);
            
            // Check if it's a new day
            String savedDate = prefs.getString("last_update_date", "");
            String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date());
            
            // If it's a new day or we need fresh data, get it from database
            if (!currentDate.equals(savedDate)) {
                try (WaterDbHelper dbHelper = new WaterDbHelper(context)) {
                    currentIntake = dbHelper.getTodayTotalIntake();
                    prefs.edit()
                        .putFloat("current_intake", currentIntake)
                        .putString("last_update_date", currentDate)
                        .apply();
                }
            }
            
            float progress = Math.min(1f, currentIntake / dailyGoal);
            
            // Update progress bar with color based on progress
            int progressColor;
            if (progress < 0.3f) {
                progressColor = context.getColor(R.color.progress_low);
            } else if (progress < 0.7f) {
                progressColor = context.getColor(R.color.progress_medium);
            } else {
                progressColor = context.getColor(R.color.progress_high);
            }
            
            // Update progress bar
            views.setProgressBar(R.id.widget_progress_bar, 100, (int)(progress * 100), false);
            
            // Update score text with color
            String scoreText = String.format(Locale.getDefault(), "%d%%", (int)(progress * 100));
            views.setTextViewText(R.id.widget_score_text, scoreText);
            views.setTextColor(R.id.widget_score_text, progressColor);
            
            // Update water droplet
            views.setImageViewResource(R.id.water_droplet_view, R.drawable.water_droplet_vector);
            
            // Set up click actions
            setupClickActions(context, views, appWidgetId);
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            
            // Schedule animation reset if needed
            if (animate) {
                scheduleAnimationReset(context, appWidgetId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating droplet widget", e);
        }
    }

    private void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
            new ComponentName(context, WaterDropletWidgetProvider.class)
        );
        
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    private void setupClickActions(Context context, RemoteViews views, int appWidgetId) {
        // Quick add action
        Intent quickAddIntent = new Intent(context, WaterDropletWidgetProvider.class)
            .setAction(ACTION_QUICK_ADD);
        PendingIntent quickAddPending = PendingIntent.getBroadcast(
            context, 
            appWidgetId, 
            quickAddIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        views.setOnClickPendingIntent(R.id.water_droplet_view, quickAddPending);
        
        // View stats action
        Intent viewStatsIntent = new Intent(context, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent viewStatsPending = PendingIntent.getActivity(
            context, 
            appWidgetId + 1000, 
            viewStatsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        views.setOnClickPendingIntent(R.id.widget_container, viewStatsPending);
    }

    private void handleQuickAdd(Context context) {
        try (WaterDbHelper dbHelper = new WaterDbHelper(context)) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            float quickAddAmount = prefs.getFloat(PREF_QUICK_ADD_AMOUNT, 250f);
            
            // Add water intake
            long id = dbHelper.addWaterIntake((int)quickAddAmount);
            if (id != -1) {
                // Update preferences with new values
                float totalIntake = dbHelper.getTodayTotalIntake();
                prefs.edit()
                    .putFloat("current_intake", totalIntake)
                    .putString("last_update_date", new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        .format(new java.util.Date()))
                    .apply();
                
                // Update all widgets
                WidgetUpdateHelper.updateAllWidgets(context);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling quick add", e);
        }
    }

    private void scheduleAnimationReset(Context context, int appWidgetId) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            updateDropletWidget(context, appWidgetManager, appWidgetId, false);
        }, ANIMATION_DURATION);

    }

    private void launchMainActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        WaterDropletWidgetProvider provider = new WaterDropletWidgetProvider();
        provider.updateDropletWidget(context, appWidgetManager, appWidgetId, false);
    }
} 