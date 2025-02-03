package com.ahsan.watertrackplus.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.ahsan.watertrackplus.data.WaterDbHelper;

public class WidgetUpdateHelper {
    public static final String ACTION_DATA_UPDATED = "com.ahsan.watertrackplus.DATA_UPDATED";

    public static void updateAllWidgets(Context context) {
        // First update shared preferences with latest data
        updateSharedPreferences(context);
        
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        
        // Update water droplet widget
        ComponentName waterDropletWidget = new ComponentName(context, WaterDropletWidgetProvider.class);
        int[] waterDropletWidgetIds = appWidgetManager.getAppWidgetIds(waterDropletWidget);
        if (waterDropletWidgetIds != null && waterDropletWidgetIds.length > 0) {
            appWidgetManager.notifyAppWidgetViewDataChanged(waterDropletWidgetIds, android.R.id.list);
            Intent updateIntent = new Intent(context, WaterDropletWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, waterDropletWidgetIds);
            context.sendBroadcast(updateIntent);
        }
        
        // Update water track widget
        ComponentName waterTrackWidget = new ComponentName(context, WaterTrackWidgetProvider.class);
        int[] waterTrackWidgetIds = appWidgetManager.getAppWidgetIds(waterTrackWidget);
        if (waterTrackWidgetIds != null && waterTrackWidgetIds.length > 0) {
            appWidgetManager.notifyAppWidgetViewDataChanged(waterTrackWidgetIds, android.R.id.list);
            Intent updateIntent = new Intent(context, WaterTrackWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, waterTrackWidgetIds);
            context.sendBroadcast(updateIntent);
        }

        // Broadcast general update action
        context.sendBroadcast(new Intent(ACTION_DATA_UPDATED));
    }

    private static void updateSharedPreferences(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(
                WaterTrackWidgetProvider.PREFS_NAME, 
                Context.MODE_PRIVATE
            );
            
            try (WaterDbHelper dbHelper = new WaterDbHelper(context)) {
                float totalIntake = dbHelper.getTodayTotalIntake();
                String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(new java.util.Date());
                
                prefs.edit()
                    .putFloat("current_intake", totalIntake)
                    .putString("last_update_date", currentDate)
                    .apply();
            }
        } catch (Exception e) {
            android.util.Log.e("WidgetUpdateHelper", "Error updating preferences", e);
        }
    }
} 