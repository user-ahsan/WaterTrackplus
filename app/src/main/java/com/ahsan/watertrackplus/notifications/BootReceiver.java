package com.ahsan.watertrackplus.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && 
            intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            
            NotificationScheduler scheduler = new NotificationScheduler(context);
            if (scheduler.isNotificationsEnabled()) {
                scheduler.scheduleAllNotifications();
            }
        }
    }
} 