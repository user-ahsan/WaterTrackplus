package com.ahsan.watertrackplus.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.ahsan.watertrackplus.MainActivity;
import com.ahsan.watertrackplus.R;
import com.ahsan.watertrackplus.data.WaterDbHelper;

public class NotificationWorker extends Worker {
    private static final String CHANNEL_ID = "water_track_reminders";
    private static final int NOTIFICATION_ID = 1;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        int hour = getInputData().getInt("notification_hour", 9);
        String userName = getInputData().getString("user_name");

        try (WaterDbHelper dbHelper = new WaterDbHelper(context)) {
            float dailyGoal = getDailyGoal(context);
            float currentIntake = dbHelper.getTodayTotalIntake();
            float progressPercentage = (currentIntake / dailyGoal) * 100;

            String message = getNotificationMessage(hour, userName, progressPercentage);
            if (message != null) {
                showNotification(context, message);
                // Reschedule for next day
                new NotificationScheduler(context).scheduleNotification(hour, getNotificationTag(hour));
            }

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private String getNotificationMessage(int hour, String userName, float progressPercentage) {
        // Don't send notification if goal is already achieved (except for congratulatory message)
        if (progressPercentage >= 100 && hour != 21) {
            if (!hasShownCongratulations()) {
                setCongratulatoryMessageShown();
                return String.format("Great job, %s! You've hit your hydration goal today. Keep it up! 🎉", userName);
            }
            return null;
        }

        switch (hour) {
            case 9: // Morning
                if (progressPercentage == 0) {
                    return String.format("%s, Don't forget to catch up on your water intake! Stay hydrated. 💧", userName);
                }
                break;
            case 12: // Midday
                if (progressPercentage == 0) {
                    return String.format("%s, It's noon! Have you had enough water today? Log your intake now!", userName);
                }
                break;
            case 15: // Afternoon
                if (progressPercentage < 50) {
                    return String.format("%s, you're halfway through the day! Let's make sure you're also halfway to your water goal! 💦", userName);
                }
                break;
            case 18: // Evening
                if (progressPercentage < 75) {
                    return String.format("%s, you're almost there! A few more sips and you'll reach your hydration goal. 🚰", userName);
                }
                break;
            case 21: // Night
                if (progressPercentage < 100) {
                    return String.format("%s, last call for hydration! Drink up before the day ends. 🌙", userName);
                }
                break;
        }
        return null;
    }

    private void showNotification(Context context, String message) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentTitle("Water Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Water Track Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Reminders to stay hydrated throughout the day");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private float getDailyGoal(Context context) {
        // Get from SharedPreferences or return default
        return context.getSharedPreferences("water_track_prefs", Context.MODE_PRIVATE)
                .getFloat("daily_goal", 2500f);
    }

    private boolean hasShownCongratulations() {
        return getApplicationContext().getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
                .getBoolean("congratulations_shown_" + java.time.LocalDate.now(), false);
    }

    private void setCongratulatoryMessageShown() {
        getApplicationContext().getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("congratulations_shown_" + java.time.LocalDate.now(), true)
                .apply();
    }

    private String getNotificationTag(int hour) {
        switch (hour) {
            case 9: return "morning_reminder";
            case 12: return "midday_reminder";
            case 15: return "afternoon_reminder";
            case 18: return "evening_reminder";
            case 21: return "night_reminder";
            default: return "water_reminder";
        }
    }
} 