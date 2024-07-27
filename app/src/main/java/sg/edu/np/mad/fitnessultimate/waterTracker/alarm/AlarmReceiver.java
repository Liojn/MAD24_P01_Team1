package sg.edu.np.mad.fitnessultimate.waterTracker.alarm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {
    static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        String message = "Time to hydrate!";
        // Show notification directly from BroadcastReceiver
        showNotification(context, message);
    }

    private void showNotification(Context context, String message) {
        createNotificationChannel(context);

        // Check if the app has permission to post notifications
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it or handle the lack of permission gracefully
            Toast.makeText(context, "Notification permission not granted. Cannot show notification.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Intent for Snooze action
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 3, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent for Dismiss action
        Intent dismissIntent = new Intent(context, DismissReceiver.class);
        dismissIntent.setAction("ACTION_DISMISS");
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 2, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        Intent intent = new Intent(context, ReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        @SuppressLint("NotificationTrampoline") NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Alarm")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_popup_reminder, "Snooze", snoozePendingIntent) // Add snooze action
                .addAction(android.R.drawable.ic_delete, "Dismiss", dismissPendingIntent)
                .setAutoCancel(false)
                .setOngoing(true); // Make the notification stay until an action is taken

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        CharSequence name = "AlarmChannel";
        String description = "Channel for alarm notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}


