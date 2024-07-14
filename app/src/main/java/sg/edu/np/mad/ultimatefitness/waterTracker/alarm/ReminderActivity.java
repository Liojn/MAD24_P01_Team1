package sg.edu.np.mad.ultimatefitness.waterTracker.alarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sg.edu.np.mad.ultimatefitness.R;
import sg.edu.np.mad.ultimatefitness.loginSignup.ProfilePageActivity;

public class ReminderActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1000;

    private RecyclerView recyclerView;
    private AlarmAdapter alarmAdapter;
    private List<AlarmClass> alarmList;
    private FloatingActionButton addAlarmButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private int selectedHour;
    private int selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigate back to ProfilePage
        findViewById(R.id.backBtn).setOnClickListener(v -> {
            Intent intent = new Intent(ReminderActivity.this, ProfilePageActivity.class);
            startActivity(intent);
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        addAlarmButton = findViewById(R.id.addAlarmButton);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        alarmList = new ArrayList<>();
        alarmAdapter = new AlarmAdapter(this, alarmList, db, auth);
        recyclerView.setAdapter(alarmAdapter);

        // Load existing alarms from Firestore
        loadAlarms();

        // Set onClickListener for addAlarmButton to show TimePickerDialog
        addAlarmButton.setOnClickListener(v -> {
            requestNotificationPermission();
            showTimePickerDialog();
        });

        createNotificationChannel();
    }

    // Method to load alarms from Firestore
    private void loadAlarms() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).collection("reminders")
                .orderBy("time", Query.Direction.DESCENDING)  // Order by time descending
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        alarmList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();
                            String time = document.getString("time");
                            List<String> repeatDayNames = (List<String>) document.get("repeat");

                            // Convert day names back to integer positions, if needed
                            List<Integer> repeatDays = convertDayNamesToIntegers(repeatDayNames);

                            boolean isEnabled = document.getBoolean("isEnabled");

                            AlarmClass alarm = new AlarmClass(time, repeatDays, isEnabled, documentId);
                            alarmList.add(alarm);

                            // Set repeating alarm
                            if (isEnabled) {
                                String[] timeParts = time.split("[: ]");
                                int hour = Integer.parseInt(timeParts[0]);
                                int minute = Integer.parseInt(timeParts[1]);
                                String period = timeParts[2];

                                if (period.equals("PM") && hour != 12) {
                                    hour += 12;
                                } else if (period.equals("AM") && hour == 12) {
                                    hour = 0;
                                }

                                setRepeatingAlarm(hour, minute, repeatDays);
                            }
                        }
                        // Reverse the list to display alarms in descending order
                        //Collections.reverse(alarmList);
                        alarmAdapter.notifyDataSetChanged();

                        // Hide disclaimer if there are alarms
                        if (!alarmList.isEmpty()) {
                            findViewById(R.id.disclaimer).setVisibility(View.GONE);
                        } else {
                            findViewById(R.id.disclaimer).setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(ReminderActivity.this, "Error getting alarms.", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    // Method to show TimePickerDialog
    private void showTimePickerDialog() {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setTitleText("Select Time");

        final MaterialTimePicker picker = builder.build();

        picker.addOnPositiveButtonClickListener(dialog -> {
            selectedHour = picker.getHour();
            selectedMinute = picker.getMinute();
            String timeFormat = (selectedHour < 12) ? "AM" : "PM";
            String time;
            if (selectedHour == 0) {
                time = String.format(Locale.getDefault(), "12:%02d %s", selectedMinute, timeFormat);
            } else if (selectedHour > 12) {
                time = String.format(Locale.getDefault(), "%02d:%02d %s", selectedHour - 12, selectedMinute, timeFormat);
            } else {
                time = String.format(Locale.getDefault(), "%d:%02d %s", selectedHour, selectedMinute, timeFormat);
            }

            // Show repeat days selection dialog after setting the time
            showRepeatDaysSelectionDialog(time, selectedHour, selectedMinute);

            // Hide disclaimer
            findViewById(R.id.disclaimer).setVisibility(View.GONE);
        });

        picker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");
    }

    private void showRepeatDaysSelectionDialog(String time, int selectedHour, int selectedMinute) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Repeat Days");

        // Define the list of days and corresponding names
        String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        boolean[] checkedDays = new boolean[daysOfWeek.length];

        // Mapping indices to Calendar constants
        int[] dayConstants = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};

        builder.setMultiChoiceItems(daysOfWeek, checkedDays, (dialog, which, isChecked) -> {
            // Update the checked state of the item
            checkedDays[which] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Create a list of selected repeat days
            List<Integer> selectedRepeatDays = new ArrayList<>();
            for (int i = 0; i < checkedDays.length; i++) {
                if (checkedDays[i]) {
                    // Add the corresponding Calendar constant
                    selectedRepeatDays.add(dayConstants[i]);
                }
            }

            // Create a new alarm object
            AlarmClass newAlarm = new AlarmClass(time, selectedRepeatDays, true, "");

            // Save the alarm to Firestore
            saveAlarmToFirestore(newAlarm);

            // Set the alarm with the selected time and repeat days
            //setRepeatingAlarm(selectedHour, selectedMinute, selectedRepeatDays);

            // Hide disclaimer
            findViewById(R.id.disclaimer).setVisibility(View.GONE);

            // Show a toast message
            Toast.makeText(ReminderActivity.this, "Alarm set for " + time, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Do nothing, cancel the selection
        });

        builder.show();
    }

    private List<String> convertRepeatDaysToNames(List<Integer> repeatDays) {
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());
        String[] weekdays = dfs.getWeekdays();
        List<String> repeatDayNames = new ArrayList<>();

        for (Integer day : repeatDays) {
            repeatDayNames.add(weekdays[day]);
        }
        return repeatDayNames;
    }

    private List<Integer> convertDayNamesToIntegers(List<String> dayNames) {
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());
        String[] weekdays = dfs.getWeekdays();
        List<Integer> repeatDays = new ArrayList<>();

        for (String dayName : dayNames) {
            for (int i = 0; i < weekdays.length; i++) {
                if (weekdays[i].equals(dayName)) {
                    repeatDays.add(i);
                    break;
                }
            }
        }
        return repeatDays;
    }

    private void setRepeatingAlarm(int hour, int minute, List<Integer> repeatDays) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);

        // Use unique requestCode for each PendingIntent
        int requestCode = 0; // Initialize with a unique value, or generate dynamically

        for (int dayOfWeek : repeatDays) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

            // Check if the selected day is in the future from the current time
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                // If the selected time has passed for today, move to the next week
                calendar.add(Calendar.DAY_OF_YEAR, 7);
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestScheduleExactAlarmsPermission();
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }


    private void saveAlarmToFirestore(AlarmClass alarm) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Convert repeat days from integers to day names
            List<String> repeatDayNames = convertRepeatDaysToNames(alarm.getRepeatDays());

            Map<String, Object> alarmData = new HashMap<>();
            alarmData.put("time", alarm.getTime());
            alarmData.put("repeat", repeatDayNames); // Use day names instead of integers
            alarmData.put("isEnabled", alarm.isEnabled());

            db.collection("users").document(userId).collection("reminders").add(alarmData)
                    .addOnSuccessListener(documentReference -> {
                        // Get the document ID and update the AlarmClass object
                        String documentId = documentReference.getId();
                        alarm.setDocumentId(documentId);

                        // Update the list and notify the adapter
                        alarmList.add(alarm);
                        alarmAdapter.notifyItemInserted(alarmList.size() - 1);

                        // Hide the disclaimer
                        findViewById(R.id.disclaimer).setVisibility(View.GONE);

                        // Set the repeating alarm
                        setRepeatingAlarm(selectedHour, selectedMinute, alarm.getRepeatDays());

                        Toast.makeText(ReminderActivity.this, "Alarm saved.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ReminderActivity.this, "Error saving alarm.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.d("ReminderActivity", "No authenticated user found.");
        }
    }

    private void requestScheduleExactAlarmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager.canScheduleExactAlarms()) {
                // Permission is already granted, no need to show the dialog
                return;
            }

            // Show a dialog explaining why the permission is needed
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("To set exact alarms, please enable the 'Schedule exact alarms' permission in the next screen.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Open the system settings for exact alarms permission
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Toast.makeText(ReminderActivity.this, "Exact alarms permission not granted. Alarm may not be accurate.", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Show a dialog explaining why the permission is needed
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("To send notifications, please enable the 'Post Notifications' permission in the next screen.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Request the notification permission
                            ActivityCompat.requestPermissions(ReminderActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(ReminderActivity.this, "Notification permission not granted. You may not receive alarms notifications.", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "AlarmChannel";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("alarmChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}

