package com.ahsan.watertrackplus.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.ahsan.watertrackplus.R;

public class WidgetUpdateWorker extends Worker {
    
    public WidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, WaterTrackWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        // Trigger widget update
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            WaterTrackWidgetProvider widgetProvider = new WaterTrackWidgetProvider();
            widgetProvider.onUpdate(context, appWidgetManager, appWidgetIds);
        }

        return Result.success();
    }
} 