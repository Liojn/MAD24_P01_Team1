package sg.edu.np.mad.ultimatefitness.waterTracker.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

public class DismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle dismiss action
        // Example: Stop any ongoing alarm sounds or services
        Intent serviceIntent = new Intent(context, AlarmSoundService.class);
        context.stopService(serviceIntent);

        // Cancel the notification if necessary
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(AlarmReceiver.NOTIFICATION_ID);
    }
}
