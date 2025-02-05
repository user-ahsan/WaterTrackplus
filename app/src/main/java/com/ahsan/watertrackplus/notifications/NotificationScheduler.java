package com.ahsan.watertrackplus.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.ahsan.watertrackplus.MainActivity;
import com.ahsan.watertrackplus.R;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";
    private static final String PREF_NAME = "notification_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_USER_NAME = "user_name";
    private static final String CHANNEL_ID = "water_track_reminders";
    private static final int WELCOME_NOTIFICATION_ID = 100;

    // Notification schedule times (24-hour format)
    private static final int MORNING_HOUR = 9;
    private static final int MIDDAY_HOUR = 12;
    private static final int AFTERNOON_HOUR = 15;
    private static final int EVENING_HOUR = 18;
    private static final int NIGHT_HOUR = 21;

    private final Context context;
    private final SharedPreferences prefs;
    private final WorkManager workManager;

    public NotificationScheduler(Context context) {
        this(context, true);
    }

    public NotificationScheduler(Context context, boolean createChannel) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.workManager = WorkManager.getInstance(context);
        if (createChannel) {
            createNotificationChannel();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Water Track Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription("Notifications for water intake tracking");
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel", e);
            }
        }
    }

    public void scheduleAllNotifications() {
        try {
            if (!isNotificationsEnabled()) {
                return;
            }

            // Show welcome notification
            showWelcomeNotification();

            scheduleNotification(MORNING_HOUR, "morning_reminder");
            scheduleNotification(MIDDAY_HOUR, "midday_reminder");
            scheduleNotification(AFTERNOON_HOUR, "afternoon_reminder");
            scheduleNotification(EVENING_HOUR, "evening_reminder");
            scheduleNotification(NIGHT_HOUR, "night_reminder");
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notifications", e);
        }
    }

    private void showWelcomeNotification() {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_water_drop)
                    .setContentTitle("Welcome to WaterTrack+")
                    .setContentText("Well done, now we can remind you to stay hydrated! 💧")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(WELCOME_NOTIFICATION_ID, builder.build());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing welcome notification", e);
        }
    }

    public void scheduleNotification(int hour, String tag) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // If the time has passed for today, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            long initialDelay = calendar.getTimeInMillis() - System.currentTimeMillis();

            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build();

            Data inputData = new Data.Builder()
                    .putInt("notification_hour", hour)
                    .putString("user_name", getUserName())
                    .build();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .addTag(tag)
                    .build();

            workManager.enqueueUniqueWork(
                    tag,
                    ExistingWorkPolicy.REPLACE,
                    notificationWork
            );
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification for hour: " + hour, e);
        }
    }

    public void cancelAllNotifications() {
        try {
            workManager.cancelAllWork();
        } catch (Exception e) {
            Log.e(TAG, "Error canceling notifications", e);
        }
    }

    public void setNotificationsEnabled(boolean enabled) {
        try {
            boolean wasEnabled = isNotificationsEnabled();
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
            
            if (enabled && !wasEnabled) {
                // Only show welcome notification when newly enabled
                showWelcomeNotification();
                scheduleAllNotifications();
            } else if (!enabled) {
                cancelAllNotifications();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting notifications enabled state", e);
        }
    }

    public boolean isNotificationsEnabled() {
        try {
            return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        } catch (Exception e) {
            Log.e(TAG, "Error checking notifications enabled state", e);
            return false;
        }
    }

    public void setUserName(String userName) {
        try {
            prefs.edit().putString(KEY_USER_NAME, userName).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error setting user name", e);
        }
    }

    private String getUserName() {
        try {
            return prefs.getString(KEY_USER_NAME, "User");
        } catch (Exception e) {
            Log.e(TAG, "Error getting user name", e);
            return "User";
        }
    }
} 