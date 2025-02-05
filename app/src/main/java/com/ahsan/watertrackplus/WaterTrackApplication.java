package com.ahsan.watertrackplus;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.ahsan.watertrackplus.notifications.NotificationScheduler;

public class WaterTrackApplication extends Application {
    private static final String TAG = "WaterTrackApplication";
    private static final String CHANNEL_ID = "water_track_reminders";
    private static final int WELCOME_NOTIFICATION_ID = 100;
    private NotificationScheduler notificationScheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            createNotificationChannel();
            
            // Initialize and schedule notifications - pass false since we already created the channel
            notificationScheduler = new NotificationScheduler(this, false);
            
            // Schedule notifications if enabled
            if (notificationScheduler.isNotificationsEnabled()) {
                notificationScheduler.scheduleAllNotifications();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing application", e);
        }
    }

    private void showWelcomeNotification() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_water_drop)
                    .setContentTitle("Welcome to WaterTrack+")
                    .setContentText("Well done, now we can remind you to stay hydrated! 💧")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = 
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(WELCOME_NOTIFICATION_ID, builder.build());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing welcome notification", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Single notification channel for all notifications
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Water Track Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription("Notifications for water intake tracking and reminders");

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channels", e);
            }
        }
    }

    public NotificationScheduler getNotificationScheduler() {
        return notificationScheduler;
    }
} 