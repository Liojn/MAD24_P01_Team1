package sg.edu.np.mad.fitnessultimate.calendarPage;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sg.edu.np.mad.fitnessultimate.BaseActivity;
import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.workoutPage.training.helpers.GlobalExerciseData;
import sg.edu.np.mad.fitnessultimate.workoutPage.training.helpers.JsonUtils;
import sg.edu.np.mad.fitnessultimate.workoutPage.training.workouts.Workout;
import sg.edu.np.mad.fitnessultimate.workoutPage.training.workouts.WorkoutActivity;

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
        setContentView(R.layout.activity_calendar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calendar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        GlobalExerciseData.getInstance().setWorkoutList(JsonUtils.loadWorkouts(this));
        GlobalExerciseData.getInstance().setExerciseList(JsonUtils.loadExercises(this));
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();
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
        getTimeSpentForDate(new FirestoreCallback() {
            @Override
            public void onCallback(Map<LocalDate, WorkoutPlan> timeSpentMap) {
                // Calendar part
                ArrayList<DayModel> daysInMonth = createDaysInMonthArray(selectedDate, timeSpentMap);
                CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, CalendarActivity.this);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
                calendarRecyclerView.setLayoutManager(layoutManager);
                calendarRecyclerView.setAdapter(calendarAdapter);

                // History part
                ArrayList<HistoryClass> workoutsList = createWorkoutsArray(timeSpentMap);
                HistoryAdapter historyAdapter = new HistoryAdapter(workoutsList, CalendarActivity.this);
                RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(CalendarActivity.this);
                historyRecyclerView.setLayoutManager(layoutManager2);
                historyRecyclerView.setAdapter(historyAdapter);
            }
        });
    }

    private ArrayList<HistoryClass> createWorkoutsArray(Map<LocalDate, WorkoutPlan> timeSpentMap) {
        ArrayList<HistoryClass> workoutsArray = new ArrayList<>();

        for (LocalDate date:timeSpentMap.keySet()) {
            if (timeSpentMap.get(date).workout != null){
                workoutsArray.add(new HistoryClass(date, timeSpentMap.get(date).workout));
                Log.d("CalendarActivity", "workout date: " + date + " workoutname: " + timeSpentMap.get(date).workout.getName());
            }
        }

        return workoutsArray;
    }

    private ArrayList<DayModel> createDaysInMonthArray(LocalDate date, Map<LocalDate, WorkoutPlan> timeSpentMap) {
        ArrayList<DayModel> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i > daysInMonth + dayOfWeek && i % 7 == 1) {
                break;
            }

            if (i <= dayOfWeek) {
                int past = yearMonth.minusMonths(1).lengthOfMonth() - dayOfWeek + i;
                LocalDate pastDate = date.minusMonths(1).withDayOfMonth(past);
                daysInMonthArray.add(new DayModel(String.valueOf(past), false, pastDate, 0, null));
            } else if (i > daysInMonth + dayOfWeek) {
                int next = i - daysInMonth - dayOfWeek;
                LocalDate nextDate = date.plusMonths(1).withDayOfMonth(next);
                daysInMonthArray.add(new DayModel(String.valueOf(next), false, nextDate, 0, null));
            } else {
                LocalDate currentDate = date.withDayOfMonth(i - dayOfWeek);

                Long timeSpent = 0L;
                Workout workout = null;
                if (timeSpentMap.containsKey(currentDate)){
                    WorkoutPlan workoutPlan = timeSpentMap.get(currentDate);
                    timeSpent = workoutPlan.timeSpent;
                    workout = workoutPlan.workout;
                }

                daysInMonthArray.add(new DayModel(String.valueOf(i - dayOfWeek), true, currentDate, timeSpent, workout));
            }
        }
        return daysInMonthArray;
    }

    private void getTimeSpentForDate(final FirestoreCallback firestoreCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference userDocRef = db.collection("users").document(userId);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    String email = document.getString("email");
                    Log.d("CalendarActivity", "email: " + email);

                    if (email != null && !email.isEmpty()) {
                        DocumentReference userTimeSpentDocRef = db.collection("timeSpentTracker").document(email);
                        CollectionReference paddingCollectionRef = userTimeSpentDocRef.collection("padding");

                        paddingCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                Map<LocalDate, WorkoutPlan> timeSpentMap = new HashMap<>();
                                List<Workout> workoutsList = GlobalExerciseData.getInstance().getWorkoutList();
                                if (task1.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task1.getResult()) {
                                        LocalDate dateId = LocalDate.parse(document.getId());
                                        Long timeSpent = document.getLong("timeSpent");

                                        Workout workout;
                                        Map<String, Object> documentData = document.getData();
                                        if (documentData == null || documentData.get("workout") == null) {
                                            workout = null;
                                            Log.d("CalendarActivity", "Workout is null");
                                        } else {
                                            Map<String, String> workoutData = (Map<String, String>) documentData.get("workout");
                                            Log.d("CalendarActivity", "testing here: " + workoutData);
                                            Log.d("CalendarActivity", "testing here: " + documentData);
                                            workout = workoutsList.stream()
                                                    .filter(e -> e.getName().equals(workoutData.get("name")))
                                                    .findFirst()
                                                    .orElse(null);

                                            Log.d("CalendarActivity", "workout here: " + workout);
                                            Log.d("CalendarActivity", "dateId: " + dateId + " timeSpent: " + timeSpent + " Workout: " + workout.getName());
                                        }

                                        WorkoutPlan workoutPlan = new WorkoutPlan(timeSpent, workout);
                                        timeSpentMap.put(dateId, workoutPlan);
                                    }
                                    firestoreCallback.onCallback(timeSpentMap);
                                } else {
                                    Log.w(TAG, "Error getting documents.", task1.getException());
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
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

    // Define the FirestoreCallback interface
    public interface FirestoreCallback {
        void onCallback(Map<LocalDate, WorkoutPlan> timeSpentMap);
    }
}
