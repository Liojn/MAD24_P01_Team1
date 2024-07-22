package sg.edu.np.mad.ultimatefitness.waterTracker.water;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sg.edu.np.mad.ultimatefitness.R;
import sg.edu.np.mad.ultimatefitness.waterTracker.alarm.ReminderActivity;

public class WaterTrackingActivity extends AppCompatActivity {
    private TextView tvProgress;
    private int currentWaterIntake = 0;
    private int userWaterGoal = 2500;
    private static final int SMALL_WATER_INCREMENT = 100; // Amount of water to increment per small droplet
    private static final int BIG_WATER_INCREMENT = 300; // Amount of water to increment per big droplet

    private RecyclerView recyclerView;
    private TextView disclaimer;
    private IntakeHistoryAdapter adapter;
    private List<IntakeHistory> intakeHistoryList;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Calendar currentWeekCalendar;
    private Calendar startOfWeek, endOfWeek;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_tracking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        tvProgress = findViewById(R.id.tvProgress);
        tvProgress.setText(String.valueOf(currentWaterIntake));

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the data and adapter
        intakeHistoryList = new ArrayList<>();
        adapter = new IntakeHistoryAdapter(intakeHistoryList);
        recyclerView.setAdapter(adapter);
        disclaimer = findViewById(R.id.disclaimer);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the real-time listener for updates
        setupRealtimeListener();
        barChart = findViewById(R.id.barChart);
        setupBarChart();
        setupRealtimeListener();

        // Fetch water intake history data from Firestore
        loadWaterIntakeHistory();

        updateGoalInProgressCircle(userWaterGoal);

        // Navigate back to ProfilePage
        findViewById(R.id.backBtn).setOnClickListener(v -> {
            Intent intent = new Intent(WaterTrackingActivity.this, ReminderActivity.class);
            startActivity(intent);
        });

        // Initialize the calendar to the current date
        currentWeekCalendar = Calendar.getInstance();
        TextView weekRangeTextView = findViewById(R.id.text_week_range);
        // Display the current week range
        updateWeekRange(weekRangeTextView);

        findViewById(R.id.button_previous_week).setOnClickListener(v -> {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            updateWeekRange(weekRangeTextView);
        });

        findViewById(R.id.button_next_week).setOnClickListener(v -> {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            updateWeekRange(weekRangeTextView);
        });

        // Bar chart click listener
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                String[] daysOfWeek = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                String selectedDay = daysOfWeek[index];

                // Fetch and display water intake history for the selected day
                fetchWaterIntakeForSelectedDay(selectedDay);
            }

            @Override
            public void onNothingSelected() {
                // Optionally handle the case when no bar is selected
            }
        });


        // Info button listener to show the info dialog
        ImageButton infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v -> showInfoDialog());

        Button btnSetGoal = findViewById(R.id.btnSetGoal);
        btnSetGoal.setOnClickListener(v -> showSetGoalDialog());

        // Find all droplet FrameLayouts
        FrameLayout smallDroplet1 = findViewById(R.id.smallDroplet1);
        FrameLayout smallDroplet2 = findViewById(R.id.smallDroplet2);
        FrameLayout smallDroplet3 = findViewById(R.id.smallDroplet3);
        FrameLayout smallDroplet4 = findViewById(R.id.smallDroplet4);

        FrameLayout bigDroplet1 = findViewById(R.id.bigDroplet1);
        FrameLayout bigDroplet2 = findViewById(R.id.bigDroplet2);
        FrameLayout bigDroplet3 = findViewById(R.id.bigDroplet3);

        // Add listeners to each droplet
        smallDroplet1.setOnTouchListener(new MyTouchListener());
        smallDroplet2.setOnTouchListener(new MyTouchListener());
        smallDroplet3.setOnTouchListener(new MyTouchListener());
        smallDroplet4.setOnTouchListener(new MyTouchListener());

        bigDroplet1.setOnTouchListener(new MyTouchListener());
        bigDroplet2.setOnTouchListener(new MyTouchListener());
        bigDroplet3.setOnTouchListener(new MyTouchListener());
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_info, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Ensure the dialog window has a custom background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnOk = dialogView.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> dialog.dismiss()); // Dismiss dialog on OK button click
    }

    private final class MyTouchListener implements View.OnTouchListener {
        private float dX, dY; // Distance from view's top-left corner to the touch point
        private float initialX, initialY; // Initial position of the droplet
        private int lastAction; // Last action performed (down, move, up)

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // Record initial touch position and view's position
                    initialX = view.getX();
                    initialY = view.getY();
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    lastAction = MotionEvent.ACTION_DOWN;
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Update view position based on touch movement
                    view.setX(event.getRawX() + dX);
                    view.setY(event.getRawY() + dY);
                    lastAction = MotionEvent.ACTION_MOVE;
                    break;

                case MotionEvent.ACTION_UP:
                    // Handle end of touch gesture
                    if (lastAction == MotionEvent.ACTION_DOWN) {
                        // Perform action when the droplet is clicked
                        Toast.makeText(getApplicationContext(), "Droplet clicked", Toast.LENGTH_SHORT).show();
                    } else {
                        // Check if droplet is dropped into the circular progress area
                        if (isViewOverlapping(view, findViewById(R.id.circleProgress))) {
                            // Determine the water increment based on the droplet type
                            int increment;
                            if ("big".equals(view.getTag())) {
                                increment = BIG_WATER_INCREMENT;
                            } else {
                                increment = SMALL_WATER_INCREMENT;
                            }

                            // Increment water intake
                            currentWaterIntake += increment;
                            tvProgress.setText(String.valueOf(currentWaterIntake));

                            // Optional: Update UI to show the progress visually
                            updateCircularProgress(currentWaterIntake);

                            // Make progress_circular visible
                            findViewById(R.id.progress_circular).setVisibility(View.VISIBLE);

                            // Add intake history
                            addIntakeHistory(String.valueOf(currentWaterIntake), increment + " ml", userWaterGoal);
                        }
                        // Reset the droplet to its initial position
                        view.setX(initialX);
                        view.setY(initialY);
                    }
                    lastAction = MotionEvent.ACTION_UP;
                    break;

                default:
                    return false;
            }

            // Prevent the touch event from being consumed by ScrollView
            view.getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
    }

    // Method to check if droplet overlaps with circular progress area
    private boolean isViewOverlapping(View firstView, View secondView) {
        Rect firstRect = new Rect();
        firstView.getHitRect(firstRect);

        Rect secondRect = new Rect();
        secondView.getHitRect(secondRect);

        return Rect.intersects(firstRect, secondRect);
    }

    private void showSetGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.set_water_goal_dialog, null);
        builder.setView(dialogView);

        NumberPicker npWaterGoal = dialogView.findViewById(R.id.npWaterGoal);

        npWaterGoal.setMinValue(10);
        npWaterGoal.setMaxValue(50);
        npWaterGoal.setValue(25);
        npWaterGoal.setWrapSelectorWheel(false);

        // Set a custom formatter to display values in hundreds
        npWaterGoal.setFormatter(value -> (value * 100) + " ml");

        // Set the default value to the current goal divided by 100
        npWaterGoal.setValue(userWaterGoal / 100);

        AlertDialog dialog = builder.create();

        Button btnSet = dialogView.findViewById(R.id.btnSet);
        btnSet.setOnClickListener(v -> {
            int goal = npWaterGoal.getValue() * 100;
            userWaterGoal = goal; // Update the user's goal

            saveGoalToFirestore(goal);

            // Save the goal or do something with it
            Toast.makeText(this, "Water goal set to " + goal + " ml", Toast.LENGTH_SHORT).show();

            // Update the progress circle goal
            updateGoalInProgressCircle(goal);

            dialog.dismiss();
        });

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Set background to transparent for rounded appearance
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    private void updateGoalInProgressCircle(int goal) {
        // Find the TextView displaying the goal
        TextView tvGoal = findViewById(R.id.tvGoal);
        tvGoal.setText("/" + goal + "ml");

        // Update the circular progress (if needed)
        updateCircularProgress(currentWaterIntake);
        updateBarChartData();
    }

    // Method to update circular progress
    private void updateCircularProgress(int currentWaterIntake) {
        // Find your circular progress view
        RelativeLayout circleProgress = findViewById(R.id.circleProgress);

        // Calculate the percentage of water intake
        float progressPercentage = (float) currentWaterIntake / userWaterGoal;

        // Set the level of the progress drawable
        int level = (int) (progressPercentage * 10000); // 10000 is the maximum level

        // Set the progress drawable
        Drawable progressDrawable = ContextCompat.getDrawable(this, R.drawable.vertical_progress_drawable);
        progressDrawable.setLevel(level);

        // Set the background of circleProgress to the progress drawable
        circleProgress.setBackground(progressDrawable);

        // Optionally, update other UI components based on progress
        TextView tvProgress = findViewById(R.id.tvProgress);
        tvProgress.setText(String.valueOf(currentWaterIntake));

        // Check if the goal is reached and show a congratulations popup
        if (currentWaterIntake >= userWaterGoal) {
            showCongratulationsPopup();
        }
    }

    private void showCongratulationsPopup() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.congratulations_toast, findViewById(R.id.custom_toast_container));

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void addIntakeHistory(String intakeAmount, String increment, int goal) {
        intakeHistoryList.add(new IntakeHistory(intakeAmount, increment, goal));
        adapter.notifyDataSetChanged();
        saveWaterIntakeToFirestore(Integer.parseInt(intakeAmount), increment, userWaterGoal);

        // Update bar chart data immediately after adding intake history
        updateBarChartData();
    }

    private void saveWaterIntakeToFirestore(int waterIntake, String increment, int goal) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> waterIntakeData = new HashMap<>();
            waterIntakeData.put("intakeAmount", waterIntake);
            waterIntakeData.put("increment", increment);
            waterIntakeData.put("time", new SimpleDateFormat("hh:mm a, MMM dd yyyy", Locale.getDefault()).format(new Date()));
            waterIntakeData.put("goal", goal);

            db.collection("users").document(userId).collection("waterTracker")
                    .add(waterIntakeData)
                    .addOnSuccessListener(documentReference -> {
                        // Document successfully written
                        Toast.makeText(WaterTrackingActivity.this, "Water intake saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to write document
                        Toast.makeText(WaterTrackingActivity.this, "Error saving water intake", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveGoalToFirestore(int goal) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> goalData = new HashMap<>();
            goalData.put("goal", goal);

            // Save the goal in a separate document or update the existing one
            db.collection("users").document(userId).collection("waterTracker").document("goal")
                    .set(goalData)
                    .addOnSuccessListener(aVoid -> {
                        // Goal successfully written
                        updateBarChartData(); // Update the bar chart data after saving the goal
                    })
                    .addOnFailureListener(e -> {
                        // Failed to write goal
                        Toast.makeText(WaterTrackingActivity.this, "Error saving water goal", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private int extractIncrementValue(String increment) {
        if (increment != null && !increment.isEmpty()) {
            // Extract the numeric value from the string
            String numericValue = increment.replaceAll("[^0-9]", "");
            try {
                return Integer.parseInt(numericValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    // Updates UI
    private void loadWaterIntakeHistory() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("waterTracker")
                    .orderBy("time", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            intakeHistoryList.clear();
                            currentWaterIntake = 0; // Reset current water intake

                            SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
                            Calendar today = Calendar.getInstance();
                            today.set(Calendar.HOUR_OF_DAY, 0);
                            today.set(Calendar.MINUTE, 0);
                            today.set(Calendar.SECOND, 0);
                            today.set(Calendar.MILLISECOND, 0);
                            Date startOfDay = today.getTime();

                            // Accumulate total intake for the current day
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int intakeAmount = document.getLong("intakeAmount").intValue();
                                String incrementStr = document.getString("increment");
                                String time = document.getString("time");
                                int goal = document.getLong("goal").intValue();

                                int increment = extractIncrementValue(incrementStr);

                                Date date = null;
                                try {
                                    date = new SimpleDateFormat("hh:mm a, MMM dd yyyy", Locale.getDefault()).parse(time);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (date != null && date.after(startOfDay)) {
                                    IntakeHistory intakeHistory = new IntakeHistory(String.valueOf(intakeAmount), incrementStr, goal);
                                    intakeHistory.setTime(time);
                                    intakeHistoryList.add(intakeHistory);

                                    // Add the increment to the total water intake for the current day
                                    currentWaterIntake += increment;
                                    userWaterGoal = goal;
                                }
                            }

                            // Update local current water intake on the main thread
                            tvProgress.setText(String.valueOf(currentWaterIntake));

                            // Update the circular progress bar based on the total intake
                            updateCircularProgress(currentWaterIntake);
                            updateGoalInProgressCircle(userWaterGoal);

                            // Notify the adapter of changes
                            adapter.notifyDataSetChanged();

                            if (intakeHistoryList.isEmpty()) {
                                disclaimer.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                disclaimer.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }

                        } else {
                            Toast.makeText(WaterTrackingActivity.this, "Error loading water intake history.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Method to update the bar chart data
    private void updateBarChartData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("waterTracker")
                    .orderBy("time", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loadBarChartData(task.getResult());
                        } else {
                            Toast.makeText(WaterTrackingActivity.this, "Error updating bar chart.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateWeekRange(TextView weekRangeTextView) {
        // Get the start and end of the week
        startOfWeek = (Calendar) currentWeekCalendar.clone();
        startOfWeek.setFirstDayOfWeek(Calendar.MONDAY);
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());

        endOfWeek = (Calendar) currentWeekCalendar.clone();
        endOfWeek.setFirstDayOfWeek(Calendar.MONDAY);
        endOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());
        endOfWeek.add(Calendar.DAY_OF_WEEK, 6);

        // Format the dates and set the text
        String weekRange = dateFormat.format(startOfWeek.getTime()) + " - " + dateFormat.format(endOfWeek.getTime());
        weekRangeTextView.setText(weekRange);

        // Load bar chart data for the new week range
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("waterTracker")
                    .orderBy("time", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Toast.makeText(WaterTrackingActivity.this, "Error loading water intake data.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots != null) {
                            loadBarChartData(snapshots);
                        }
                    });
        }
    }

    private void fetchWaterIntakeForSelectedDay(String selectedDay) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("waterTracker")
                    .orderBy("time", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            intakeHistoryList.clear();
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int intakeAmount = document.getLong("intakeAmount").intValue();
                                String increment = document.getString("increment");
                                String time = document.getString("time");
                                int goal = document.getLong("goal").intValue();

                                // Parse the time to get the date
                                Date date = null;
                                try {
                                    date = new SimpleDateFormat("hh:mm a, MMM dd yyyy", Locale.getDefault()).parse(time);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (date != null) {
                                    String dayOfWeek = sdf.format(date);
                                    if (dayOfWeek.equals(selectedDay)) {
                                        IntakeHistory intakeHistory = new IntakeHistory(String.valueOf(intakeAmount), increment, goal);
                                        intakeHistory.setTime(time);
                                        intakeHistoryList.add(intakeHistory);
                                    }
                                }
                            }

                            // Notify the adapter of changes
                            adapter.notifyDataSetChanged();

                            if (intakeHistoryList.isEmpty()) {
                                disclaimer.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                disclaimer.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(WaterTrackingActivity.this, "Error loading water intake history.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawValueAboveBar(true);

        // Customizations for the XAxis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));

        // Customizations for the YAxis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0); // Start Y-axis from 0
        leftAxis.setAxisMaximum(5000); // Set maximum value for Y-axis
        leftAxis.setGranularity(250f); // Set granularity to 250 units
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setupRealtimeListener() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("waterTracker")
                    .orderBy("time", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Toast.makeText(WaterTrackingActivity.this, "Error loading water intake data.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots != null) {
                            runOnUiThread(() -> {
                                loadBarChartData(snapshots);
                                loadWaterIntakeHistory();
                            });
                        }
                    });

            // Add another listener for the goal document if it's stored separately
            db.collection("users").document(userId).collection("waterTracker").document("goal")
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            Toast.makeText(WaterTrackingActivity.this, "Error loading goal data.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            userWaterGoal = documentSnapshot.getLong("goal").intValue(); // Assuming goal is stored under "goal"
                            runOnUiThread(() -> {
                                //loadBarChartData(snapshots); // Reuse the snapshot data but update the goal
                                updateGoalInProgressCircle(userWaterGoal); // Ensure this uses the latest goal
                            });
                        } else {
                            userWaterGoal = 2500; // Default goal value if document doesn't exist
                        }
                    });
        }
    }

    private void loadBarChartData(QuerySnapshot snapshots) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("hh:mm a, MMM dd yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        Map<String, Integer> waterIntakeMap = new HashMap<>();
        int totalWaterIntake = 0; // To calculate the total water intake for the week
        int totalDaysInWeek = 7;

        // Log the number of documents fetched
        Log.d("Firebase", "Number of documents: " + snapshots.size());

        // Get the current week and year
        int currentWeek = currentWeekCalendar.get(Calendar.WEEK_OF_YEAR);
        int currentYear = currentWeekCalendar.get(Calendar.YEAR);

        // Process fetched data
        for (QueryDocumentSnapshot document : snapshots) {
            if (document.contains("intakeAmount") && document.contains("time") && document.contains("increment")) {
                int intakeAmount = document.getLong("intakeAmount").intValue();
                String incrementStr = document.getString("increment");
                String time = document.getString("time");

                // Log the fetched data
                Log.d("Firebase", "Fetched data: intakeAmount=" + intakeAmount + ", time=" + time);

                // Parse the time to get the date
                Date date = null;
                try {
                    date = fullDateFormat.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e("ParsingError", "Error parsing date: " + time, e);
                }

                if (date != null) {
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.setTime(date);
                    int weekOfYear = dateCalendar.get(Calendar.WEEK_OF_YEAR);
                    int year = dateCalendar.get(Calendar.YEAR);

                    // Check if the date is within the current week
                    if (weekOfYear == currentWeek && year == currentYear) {
                        String dayOfWeek = dayFormat.format(date);

                        // Update the map with the intake amount for each day
                        int incrementValue = extractIncrementValue(incrementStr);
                        waterIntakeMap.put(dayOfWeek, waterIntakeMap.getOrDefault(dayOfWeek, 0) + incrementValue);

                        // Add to total intake for weekly average calculation
                        totalWaterIntake += incrementValue;
                    }
                }
            } else {
                Log.e("DataError", "Document missing required fields: " + document.getId());
            }
        }

        // Log the aggregated data
        for (Map.Entry<String, Integer> entry : waterIntakeMap.entrySet()) {
            Log.d("AggregatedData", "Day: " + entry.getKey() + ", Intake: " + entry.getValue());
        }

        // Set up the bar entries for Monday to Sunday
        String[] daysOfWeek = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            String day = daysOfWeek[i];
            int intakeAmount = waterIntakeMap.getOrDefault(day, 0);
            // Apply the goal only on the specific day of intake
            int goal = (intakeAmount > 0 && day.equals(getDayOfWeekFromDate(Calendar.getInstance().getTime()))) ? userWaterGoal : 0;
            entries.add(new BarEntry(i, new float[]{intakeAmount, goal}));
        }

        // Calculate the daily average
        double dailyAverage = totalDaysInWeek > 0 ? (double) totalWaterIntake / totalDaysInWeek : 0;
        TextView dailyAverageTextView = findViewById(R.id.dailyAverage); // Display the daily average in the TextVie
        dailyAverageTextView.setText(String.format("Daily Average: %.1f ml", dailyAverage));


        // Calculate the weekly average
        int daysWithIntake = waterIntakeMap.size();
        double weeklyAverage = daysWithIntake > 0 ? (double) totalWaterIntake / daysWithIntake : 0;
        TextView weeklyAverageTextView = findViewById(R.id.weeklyAverage);  // Display the weekly average in the TextView
        weeklyAverageTextView.setText(String.format("Weekly Average: %.1f ml", weeklyAverage));

        // Set up the dataset
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#add8e6"), Color.parseColor("#D6EBF2")}); // Example colors
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setStackLabels(new String[]{"Water Intake", "Goal"});

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barData.setBarWidth(0.7f);

        // Customize the chart as needed
        Legend legend = barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setYOffset(15f);
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(10f);
        legend.setXEntrySpace(15f);
        legend.setFormToTextSpace(10f);
        legend.setEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        xAxis.setLabelCount(daysOfWeek.length);
        xAxis.setDrawGridLines(false);

        barChart.setExtraBottomOffset(7f);
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    private String getDayOfWeekFromDate(Date date) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        return dayFormat.format(date);
    }
}