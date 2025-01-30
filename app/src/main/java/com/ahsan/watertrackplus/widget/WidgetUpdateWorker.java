package com.ahsan.watertrackplus.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WidgetUpdateWorker extends Worker {
    private static final String TAG = "WidgetUpdateWorker";

    public WidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public @NonNull Result doWork() {
        try {
            Context context = getApplicationContext();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName widgetComponent = new ComponentName(context, WaterTrackWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

            // Trigger widget update
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                Intent updateIntent = new Intent(context, WaterTrackWidgetProvider.class)
                        .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                context.sendBroadcast(updateIntent);
                Log.d(TAG, "Widget update broadcast sent");
                return Result.success();
            }
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error updating widget", e);
            return Result.retry();
        }
    }
} 