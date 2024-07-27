package sg.edu.np.mad.fitnessultimate.calendarPage;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sg.edu.np.mad.fitnessultimate.training.helpers.GlobalExerciseData;
import sg.edu.np.mad.fitnessultimate.training.workouts.Workout;

public class MiscCalendar extends BaseActivity{
    // create days in month array
    public static ArrayList<DayModel> createDaysInMonthArray(LocalDate date, Map<LocalDate, RetrievedData> dateDataMap) {
        ArrayList<DayModel> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        int counter = 0;
        for (int i = 1; i <= 42; i++) {
            if (i > daysInMonth + dayOfWeek && i % 7 == 1) {
                break;
            }

            if (i <= dayOfWeek) {
                int past = yearMonth.minusMonths(1).lengthOfMonth() - dayOfWeek + i;
                LocalDate pastDate = date.minusMonths(1).withDayOfMonth(past);
                daysInMonthArray.add(new DayModel(String.valueOf(past), false, pastDate, 0, null));
                counter += 1;
            } else if (i > daysInMonth + dayOfWeek) {
                int next = i - daysInMonth - dayOfWeek;
                LocalDate nextDate = date.plusMonths(1).withDayOfMonth(next);
                daysInMonthArray.add(new DayModel(String.valueOf(next), false, nextDate, 0, null));
            } else {
                LocalDate currentDate = date.withDayOfMonth(i - dayOfWeek);

                Long timeSpent = 0L;
                Workout workout = null;
                if (dateDataMap.containsKey(currentDate)){
                    RetrievedData retrievedData = dateDataMap.get(currentDate);
                    if (retrievedData.timeSpent == null) {
                        timeSpent = 0L;
                    } else {
                        timeSpent = retrievedData.timeSpent;
                    }
                    workout = retrievedData.workout;
                }

                daysInMonthArray.add(new DayModel(String.valueOf(i - dayOfWeek), true, currentDate, timeSpent, workout));
            }
        }
        if (counter == 7){
            daysInMonthArray = new ArrayList<>(daysInMonthArray.subList(7, daysInMonthArray.size()));
        }
        return daysInMonthArray;
    }

    // get time spent for date
    public static void getTimeSpentForDate(final MiscCalendar.FirestoreCallback firestoreCallback) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DocumentReference userDocRef = db.collection("users").document(userId);
            userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String email = document.getString("email");
                        Log.d("MiscCalendar", "email: " + email);

                        if (email != null && !email.isEmpty()) {
                            DocumentReference userTimeSpentDocRef = db.collection("timeSpentTracker").document(email);
                            CollectionReference paddingCollectionRef = userTimeSpentDocRef.collection("padding");

                            paddingCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                    Map<LocalDate, RetrievedData> dateDataMap = new HashMap<>();
                                    if (task1.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task1.getResult()) {
                                            LocalDate dateId = LocalDate.parse(document.getId());
                                            Long timeSpent = document.getLong("timeSpent");

                                            Workout workout = null;
                                            Map<String, Object> documentData = document.getData();
                                            if (documentData == null || documentData.get("workout") == null) {
                                                workout = null;
//                                                Log.d("MiscCalendar", "Workout is null");
                                            } else {
                                                Map<String, String> workoutData = (Map<String, String>) documentData.get("workout");
                                                List<Workout> workoutsList = GlobalExerciseData.getInstance().getWorkoutList();

                                                workout = workoutsList.stream()
                                                        .filter(e -> e.getName().equals(workoutData.get("name")))
                                                        .findFirst()
                                                        .orElse(null);

//                                                if (workout != null){
//                                                    Log.d("MiscCalendar", "dateId: " + dateId + " timeSpent: " + timeSpent + " Workout: " + workout.getName());
//                                                }
                                            }

                                            RetrievedData retrievedData = new RetrievedData(timeSpent, workout);
                                            dateDataMap.put(dateId, retrievedData);
                                        }
                                        firestoreCallback.onCallback(dateDataMap);
                                    } else {
                                        Log.w("MiscCalendar", "Error getting documents.", task1.getException());
                                    }
                                }
                            });
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e("MiscCalendar", "Error: ", e);
        }
    }

    // Define the FirestoreCallback interface
    public interface FirestoreCallback {
        void onCallback(Map<LocalDate, RetrievedData> dateDataMap);
    }

    // Color of cell
    public static int getColorForTimeSpent(long timeSpent) {
        // Replace this logic with your desired color coding
        if (timeSpent < 30) {
            return Color.TRANSPARENT;
        } else if (timeSpent < 600) {
            return Color.rgb(159, 166, 212);
//            return Color.rgb(180, 237, 180);
        } else if (timeSpent < 1800) {
            return Color.rgb(109, 118, 181);
//            return Color.rgb(98, 227, 98);
        } else {
            return Color.rgb(73, 82, 138);
//            return Color.rgb(36, 200, 36);
        }
    }
}
