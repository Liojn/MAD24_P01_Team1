package sg.edu.np.mad.fitnessultimate.calendarPage;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.Map;

import sg.edu.np.mad.fitnessultimate.BaseActivity;
import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.BaseActivity;

public class CalendarActivity extends BaseActivity implements CalendarAdapter.OnItemListener {

    private static final String TAG = "CalendarActivity";
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
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
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        View myCircleView2 = findViewById(R.id.colorCircle1);
        changeCircleColor(myCircleView2, Color.rgb(180, 237, 180)); // Changing color to green
        View myCircleView3 = findViewById(R.id.colorCircle2);
        changeCircleColor(myCircleView3, Color.rgb(98, 227, 98)); // Changing color to green
        View myCircleView4 = findViewById(R.id.colorCircle3);
        changeCircleColor(myCircleView4, Color.rgb(36, 200, 36)); // Changing color to green
    }

    private void changeCircleColor(View view, int color) {
        GradientDrawable background = (GradientDrawable) view.getBackground();
        background.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        getTimeSpentForDate(new FirestoreCallback() {
            @Override
            public void onCallback(Map<LocalDate, Long> timeSpentMap) {
                ArrayList<DayModel> daysInMonth = createDaysInMonthArray(selectedDate, timeSpentMap);
                CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, CalendarActivity.this);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
                calendarRecyclerView.setLayoutManager(layoutManager);
                calendarRecyclerView.setAdapter(calendarAdapter);
            }
        });
    }

    private ArrayList<DayModel> createDaysInMonthArray(LocalDate date, Map<LocalDate, Long> timeSpentMap) {
        ArrayList<DayModel> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            Log.i(TAG, String.valueOf(i));
            if (i > daysInMonth + dayOfWeek && i % 7 == 1) {
                break;
            }

            if (i <= dayOfWeek) {
                int past = yearMonth.minusMonths(1).lengthOfMonth() - dayOfWeek + i;
                LocalDate pastDate = date.minusMonths(1).withDayOfMonth(past);
                daysInMonthArray.add(new DayModel(String.valueOf(past), false, pastDate, 0));
            } else if (i > daysInMonth + dayOfWeek) {
                int next = i - daysInMonth - dayOfWeek;
                LocalDate nextDate = date.plusMonths(1).withDayOfMonth(next);
                daysInMonthArray.add(new DayModel(String.valueOf(next), false, nextDate, 0));
            } else {
                LocalDate currentDate = date.withDayOfMonth(i - dayOfWeek);
                long timeSpent = timeSpentMap.getOrDefault(currentDate, 0L);
                daysInMonthArray.add(new DayModel(String.valueOf(i - dayOfWeek), true, currentDate, timeSpent));
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
                                Map<LocalDate, Long> timeSpentMap = new HashMap<>();
                                if (task1.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task1.getResult()) {
                                        LocalDate dateId = LocalDate.parse(document.getId());
                                        Long timeSpent = document.getLong("timeSpent");
                                        Log.d("CalendarActivity", "dateId: " + dateId + " timeSpent: " + timeSpent);
                                        timeSpentMap.put(dateId, timeSpent);
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
        }
    }

    // Define the FirestoreCallback interface
    public interface FirestoreCallback {
        void onCallback(Map<LocalDate, Long> timeSpentMap);
    }
}
