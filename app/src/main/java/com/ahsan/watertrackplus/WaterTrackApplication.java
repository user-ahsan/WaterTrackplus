package com.ahsan.watertrackplus;

import android.Manifest;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
        
        // Show welcome notification if notifications are enabled
        if (notificationScheduler.isNotificationsEnabled() && areNotificationsPermitted()) {
            showWelcomeNotification();
            notificationScheduler.scheduleAllNotifications();
        }
    }

    private boolean areNotificationsPermitted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(this, 
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permissions are granted by default for older Android versions
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
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            
            // Check permission again just before showing notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this, 
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            
            notificationManager.notify(WELCOME_NOTIFICATION_ID, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Main app notification channel
            NotificationChannel mainChannel = new NotificationChannel(
                CHANNEL_ID,
                "Water Track Notifications",
                NotificationManager.IMPORTANCE_HIGH
            );
            mainChannel.setDescription("Notifications for water intake tracking");
            mainChannel.enableLights(true);
            mainChannel.enableVibration(true);
            mainChannel.setShowBadge(true);

            // Reminders notification channel
            NotificationChannel reminderChannel = new NotificationChannel(
                "water_track_reminders",
                "Water Track Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setDescription("Daily water intake reminders");
            reminderChannel.enableLights(true);
            reminderChannel.enableVibration(true);
            reminderChannel.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(mainChannel);
            notificationManager.createNotificationChannel(reminderChannel);
        }
    }

    public NotificationScheduler getNotificationScheduler() {
        return notificationScheduler;
    }
} 