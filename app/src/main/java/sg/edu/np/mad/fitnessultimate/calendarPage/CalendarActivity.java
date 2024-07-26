package sg.edu.np.mad.fitnessultimate.calendarPage;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;


import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.training.helpers.GlobalExerciseData;
import sg.edu.np.mad.fitnessultimate.training.helpers.JsonUtils;
import sg.edu.np.mad.fitnessultimate.training.workouts.WorkoutActivity;

public class CalendarActivity extends BaseActivity implements CalendarAdapter.OnItemListener, HistoryAdapter.OnItemListener2 {

    private static final String TAG = "CalendarActivity";
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private RecyclerView historyRecyclerView;
    private LocalDate selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        this.setContentView(R.layout.activity_calendar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calendar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // populate singleton class with workout list
        GlobalExerciseData.getInstance().setWorkoutList(JsonUtils.loadWorkouts(this));
        GlobalExerciseData.getInstance().setExerciseList(JsonUtils.loadExercises(this));

        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();

        //Back button Intent
        findViewById(R.id.Back).setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        View myCircleView2 = findViewById(R.id.colorCircle1);
        changeCircleColor(myCircleView2, Color.rgb(159, 166, 212));
        View myCircleView3 = findViewById(R.id.colorCircle2);
        changeCircleColor(myCircleView3, Color.rgb(109, 118, 181));
        View myCircleView4 = findViewById(R.id.colorCircle3);
        changeCircleColor(myCircleView4, Color.rgb(73, 82, 138));
    }

    private void changeCircleColor(View view, int color) {
        GradientDrawable background = (GradientDrawable) view.getBackground();
        background.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        MiscCalendar.getTimeSpentForDate(new MiscCalendar.FirestoreCallback() {
            @Override
            public void onCallback(Map<LocalDate, RetrievedData> dateDataMap) {
                // Calendar part
                ArrayList<DayModel> daysInMonth = MiscCalendar.createDaysInMonthArray(selectedDate, dateDataMap);
                CalendarAdapter calendarAdapter = new CalendarAdapter(CalendarActivity.this, daysInMonth, CalendarActivity.this);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
                calendarRecyclerView.setLayoutManager(layoutManager);
                calendarRecyclerView.setAdapter(calendarAdapter);

                // History part
                ArrayList<HistoryClass> workoutsList = createWorkoutsArray(dateDataMap);
                HistoryAdapter historyAdapter = new HistoryAdapter(workoutsList, CalendarActivity.this);
                RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(CalendarActivity.this);
                historyRecyclerView.setLayoutManager(layoutManager2);
                historyRecyclerView.setAdapter(historyAdapter);
            }
        });
    }

    private ArrayList<HistoryClass> createWorkoutsArray(Map<LocalDate, RetrievedData> dateDataMap) {
        ArrayList<HistoryClass> workoutsArray = new ArrayList<>();

        for (LocalDate date:dateDataMap.keySet()) {
            if (dateDataMap.get(date).workout != null){
                workoutsArray.add(new HistoryClass(date, dateDataMap.get(date).workout));
            }
        }

        return workoutsArray;
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, DayModel dayModel) {
        if (!dayModel.dayText.equals("")) {
            String message = "Selected Date " + dayModel.fullDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            AlertDialog alert = buildAlert(dayModel).create();
            alert.show();
        }
    }

    public AlertDialog.Builder buildAlert(DayModel dayModel) {
        // Create alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(dayModel.fullDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        // Inflate the custom layout
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);

        // Set the workout details in the custom layout
        TextView alertMessage = dialogView.findViewById(R.id.alertMessage);

        String message;
        if (dayModel.workout == null) {
            message = "No Workouts Found";
        } else {
            message = "Workout: " + dayModel.workout.getName() + "\nDescription: " + dayModel.workout.getDescription();
            // Ensure the text does not go beyond 2 lines
            alertMessage.setSingleLine(false);
            alertMessage.setEllipsize(TextUtils.TruncateAt.END);
            alertMessage.setMaxLines(2);
            alertMessage.setText(message);
        }

        alertMessage.setText(message);
        builder.setCancelable(true);
        builder.setNegativeButton("CLOSE", (dialog, which) -> {

        });

        return builder;
    }

    @Override
    public void onItemClick2(int position, HistoryClass historyClass) {
        String message = historyClass.workout.getName();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        Log.d(TAG, "this is the workout" + historyClass.workout);
        Log.d(TAG, "this is the workout" + historyClass.workout.getExercises().get(0).getName());

        //intent
        Intent intent = new Intent(CalendarActivity.this, WorkoutActivity.class);
        intent.putExtra("workout", historyClass.workout);
        startActivity(intent);
    }
}
