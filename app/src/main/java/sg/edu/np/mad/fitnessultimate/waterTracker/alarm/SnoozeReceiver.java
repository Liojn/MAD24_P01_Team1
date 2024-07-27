package sg.edu.np.mad.fitnessultimate.waterTracker.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

public class SnoozeReceiver extends BroadcastReceiver {
    private static final int SNOOZE_DURATION = 60 * 60 * 1000; // 60 minutes in milliseconds

    @Override
    public void onReceive(Context context, Intent intent) {
        // Display a toast to inform the user that the alarm is snoozed
        Toast.makeText(context, "Alarm snoozed for 2 minutes", Toast.LENGTH_SHORT).show();

        // Set up the snooze
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Check if the app can schedule exact alarms
        if (alarmManager.canScheduleExactAlarms()) {
            try {
                // Set the alarm to trigger again after the snooze duration
                long snoozeTime = System.currentTimeMillis() + SNOOZE_DURATION;
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
            } catch (SecurityException e) {
                // Handle the lack of permission gracefully
                Toast.makeText(context, "Cannot schedule exact alarm. Please enable exact alarms in settings.", Toast.LENGTH_LONG).show();
                // Optionally, open settings for the user to enable the permission
                Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(settingsIntent);
            }
        } else {
            // Handle the case where the app cannot schedule exact alarms
            Toast.makeText(context, "Cannot schedule exact alarm. Please enable exact alarms in settings.", Toast.LENGTH_LONG).show();
            // Optionally, open settings for the user to enable the permission
            Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            context.startActivity(settingsIntent);
        }
    }
}
