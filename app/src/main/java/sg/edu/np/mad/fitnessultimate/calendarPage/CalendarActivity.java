package sg.edu.np.mad.fitnessultimate.calendarPage;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import sg.edu.np.mad.fitnessultimate.BaseActivity;
import sg.edu.np.mad.fitnessultimate.R;

public class CalendarActivity extends BaseActivity implements CalendarAdapter.OnItemListener
{
    private static final String TAG = "CalendarActivity";
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    String email;


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

    private void initWidgets()
    {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<DayModel> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<DayModel> daysInMonthArray(LocalDate date)
    {
        ArrayList<DayModel> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++) {
            Log.i(TAG, String.valueOf(i));
            // Break if > then weeks needed
            if (i > daysInMonth + dayOfWeek && i % 7 == 1){
                break;
            }

            // Get time spent for date
            int timeSpent = getTimeSpentForDate(date);

            // Add days before start of month
            if(i <= dayOfWeek) {
                int past = yearMonth.minusMonths(1).lengthOfMonth() - dayOfWeek + i;
                LocalDate pastDate = selectedDate.minusMonths(1).withDayOfMonth(past);
                daysInMonthArray.add(new DayModel(String.valueOf(past), false, pastDate));
            }
            // Add days after end of month
            else if (i > daysInMonth + dayOfWeek) {
                int next = i - daysInMonth - dayOfWeek;
                LocalDate nextDate = selectedDate.plusMonths(1).withDayOfMonth(next);
                daysInMonthArray.add(new DayModel(String.valueOf(next), false, nextDate));
            }
            // Add days before start of month
            else {
                LocalDate currentDate = selectedDate.withDayOfMonth(i - dayOfWeek);
                daysInMonthArray.add(new DayModel(String.valueOf(i - dayOfWeek), true, currentDate));
            }
        }
        return daysInMonthArray;
    }

    private int getTimeSpentForDate(LocalDate date) {
        // Replace this with your logic to get time spent data for the given date

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference userDocRef = db.collection("users").document(userId);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    email = document.getString("email");
                }
            }
        });

        CollectionReference docRef = db.collection("timeSpentTracker" + email + "/padding");
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dateId = document.getId();
                            Long timeSpent = document.getLong("timeSpent");

                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        return 0;
    }

    private String monthYearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction(View view)
    {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view)
    {
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
}