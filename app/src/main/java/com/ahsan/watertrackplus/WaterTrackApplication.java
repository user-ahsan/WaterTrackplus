package com.ahsan.watertrackplus;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.ahsan.watertrackplus.notifications.NotificationScheduler;

public class WaterTrackApplication extends Application {
    private static final String CHANNEL_ID = "water_track";
    private static final int WELCOME_NOTIFICATION_ID = 100;
    private NotificationScheduler notificationScheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        
        // Initialize and schedule notifications
        notificationScheduler = new NotificationScheduler(this);
        
        // Schedule notifications if enabled
        if (notificationScheduler.isNotificationsEnabled()) {
            notificationScheduler.scheduleAllNotifications();
        }
    }

    private void showWelcomeNotification() {
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
        notificationManager.notify(WELCOME_NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Main app notification channel
            NotificationChannel mainChannel = new NotificationChannel(
                CHANNEL_ID,
                "Water Track Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            mainChannel.setDescription("Notifications for water intake tracking");

            // Reminders notification channel
            NotificationChannel reminderChannel = new NotificationChannel(
                "water_track_reminders",
                "Water Track Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            reminderChannel.setDescription("Daily water intake reminders");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(mainChannel);
            notificationManager.createNotificationChannel(reminderChannel);
        }
    }

    public NotificationScheduler getNotificationScheduler() {
        return notificationScheduler;
    }
} 