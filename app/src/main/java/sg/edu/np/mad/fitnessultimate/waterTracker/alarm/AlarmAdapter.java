package sg.edu.np.mad.fitnessultimate.waterTracker.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sg.edu.np.mad.fitnessultimate.R;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<AlarmClass> alarmList;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public AlarmAdapter(Context context, List<AlarmClass> alarmList, FirebaseFirestore db, FirebaseAuth auth) {
        this.context = context;
        this.alarmList = alarmList;
        this.db = db;
        this.auth = auth;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.alarm_card_item, parent, false);
        return new AlarmViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        if (position < alarmList.size()) {
            AlarmClass alarm = alarmList.get(position);
            holder.alarmTime.setText(alarm.getTime());
            // Convert repeat days to names for display
            List<Integer> repeatDays = alarm.getRepeatDays();
            List<String> repeatDayNames = convertRepeatDaysToNames(repeatDays);

            String repeatText = "Repeat: " + TextUtils.join(", ", repeatDayNames);
            holder.alarmRepeat.setText(repeatText);
            holder.alarmSwitch.setChecked(alarm.isEnabled());

            // Set onClickListener for menu icon
            holder.menuIcon.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    showPopupMenu(v, adapterPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    private List<String> convertRepeatDaysToNames(List<Integer> repeatDays) {
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());
        String[] shortWeekdays = dfs.getShortWeekdays();
        List<String> repeatDayNames = new ArrayList<>();

        for (Integer day : repeatDays) {
            repeatDayNames.add(shortWeekdays[day]);
        }
        return repeatDayNames;
    }

    private List<String> convertRepeatDaysToNamesLong(List<Integer> repeatDays) {
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());
        String[] weekdays = dfs.getWeekdays();
        List<String> repeatDayNames = new ArrayList<>();

        for (Integer day : repeatDays) {
            repeatDayNames.add(weekdays[day]);
        }
        return repeatDayNames;
    }

    private void showPopupMenu(View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.menu_alarm_options);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    editAlarm(position);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    deleteAlarm(position);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void editAlarm(final int position) {
        final AlarmClass alarm = alarmList.get(position);

        // Split the time string to get hours, minutes, and period
        String[] timeParts = alarm.getTime().split(":");
        int currentHour = Integer.parseInt(timeParts[0]);
        int currentMinute = Integer.parseInt(timeParts[1].split(" ")[0]);
        String currentPeriod = timeParts[1].split(" ")[1];

        // Adjust hour based on AM/PM
        if (currentPeriod.equals("PM") && currentHour != 12) {
            currentHour += 12;
        } else if (currentPeriod.equals("AM") && currentHour == 12) {
            currentHour = 0;
        }

        // Build the MaterialTimePicker with the current alarm time as default
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setTitleText("Edit Alarm Time");

        final MaterialTimePicker picker = builder.build();

        picker.addOnPositiveButtonClickListener(dialog -> {
            int selectedHour = picker.getHour();
            int selectedMinute = picker.getMinute();
            String period = (selectedHour < 12) ? "AM" : "PM";
            int hour = (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12;
            String newTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, selectedMinute, period);

            alarm.setTime(newTime);
            notifyItemChanged(position);

            // Update the alarm in Firestore
            updateAlarmInFirestore(alarm);

            // Reset the system alarm with the new time and repeat days
            resetSystemAlarm(alarm, selectedHour, selectedMinute);

            Toast.makeText(context, "Alarm time updated to " + newTime, Toast.LENGTH_SHORT).show();
        });

        picker.show(((FragmentActivity) context).getSupportFragmentManager(), "MATERIAL_TIME_PICKER");
    }

    private void deleteAlarm(int position) {
        AlarmClass alarm = alarmList.get(position);
        String userId = auth.getCurrentUser().getUid();
        String documentId = alarm.getDocumentId();

        if (documentId != null && !documentId.isEmpty()) {
            db.collection("users").document(userId).collection("reminders").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        alarmList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Alarm for " + alarm.getTime() + " deleted", Toast.LENGTH_SHORT).show();

                        // Show the disclaimer if there are no more alarms
                        if (alarmList.isEmpty()) {
                            ((ReminderActivity) context).findViewById(R.id.disclaimer).setVisibility(View.VISIBLE);
                        }

                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error deleting alarm", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Document ID is not available for this alarm.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetSystemAlarm(AlarmClass alarm, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarm.getDocumentId().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Cancel the previous alarm if it exists
        alarmManager.cancel(pendingIntent);

        // Set the new alarm for each selected repeat day
        for (int dayOfWeek : alarm.getRepeatDays()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

            // Check if the alarm time has already passed for today
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                // Move to the next week if the alarm time has passed
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void updateAlarmInFirestore(AlarmClass alarm) {
        String userId = auth.getCurrentUser().getUid();
        String documentId = alarm.getDocumentId();

        // Convert repeat days from integers to day names
        List<String> repeatDayNames = convertRepeatDaysToNamesLong(alarm.getRepeatDays());

        if (documentId != null && !documentId.isEmpty()) {
            Map<String, Object> updatedAlarmData = new HashMap<>();
            updatedAlarmData.put("time", alarm.getTime());
            updatedAlarmData.put("repeat", repeatDayNames);
            updatedAlarmData.put("isEnabled", alarm.isEnabled());

            db.collection("users").document(userId).collection("reminders").document(documentId)
                    .update(updatedAlarmData)
                    //.addOnSuccessListener(aVoid -> Toast.makeText(context, "Alarm " + alarm.getTime() + " updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Error updating alarm in Firestore", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Document ID is not available for this alarm.", Toast.LENGTH_SHORT).show();
        }
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView alarmTime, alarmRepeat;
        ImageView menuIcon;
        Switch alarmSwitch;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmTime = itemView.findViewById(R.id.alarm_item);
            alarmRepeat = itemView.findViewById(R.id.alarm_repeat);
            alarmSwitch = itemView.findViewById(R.id.alarm_switch);
            menuIcon = itemView.findViewById(R.id.menu_icon);
        }
    }
}