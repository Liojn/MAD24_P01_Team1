package sg.edu.np.mad.fitnessultimate;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {

    private static TimeTracker timeTracker = new TimeTracker();

    @Override
    protected void onResume() {
        super.onResume();
        timeTracker.startTracking();

        // Save the start time in shared preferences or another persistent storage
        SharedPreferences preferences = getSharedPreferences("TimeTrackerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("startTime", System.currentTimeMillis());
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        String userId = fAuth.getCurrentUser().getUid();

        // Get the start time from shared preferences
        SharedPreferences preferences = getSharedPreferences("TimeTrackerPrefs", MODE_PRIVATE);
        long startTime = preferences.getLong("startTime", System.currentTimeMillis());

        long currentTime = System.currentTimeMillis();
        Date startDate = new Date(startTime);
        Date currentDate = new Date(currentTime);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDateString = sdf.format(startDate);
        String currentDateString = sdf.format(currentDate);

        // Get user's email
        DocumentReference userDocRef = db.collection("users").document(userId);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    String email = document.getString("email");
                    Log.d("BaseActivity", "email: " + email);
                    if (email != null) {
                        if (startDateString.equals(currentDateString)) {
                            // Same day, update timeSpent for the same date
                            updateTimeSpent(db, email + "/padding/" + startDateString, startTime, currentTime);
                        } else {
                            // Different days, split the time spent
                            // Update timeSpent for the start day
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(startDate);
                            calendar.set(Calendar.HOUR_OF_DAY, 23);
                            calendar.set(Calendar.MINUTE, 59);
                            calendar.set(Calendar.SECOND, 59);
                            long endOfStartDay = calendar.getTimeInMillis();

                            updateTimeSpent(db, email + "/padding/" + startDateString, startTime, endOfStartDay);

                            // Update timeSpent for the current day
                            calendar.setTime(currentDate);
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            long startOfCurrentDay = calendar.getTimeInMillis();

                            updateTimeSpent(db, email + "/padding/" + currentDateString, startOfCurrentDay, currentTime);
                        }
                    }
                }
            }
        });
    }

    private void updateTimeSpent(FirebaseFirestore db, String documentPath, long startTime, long endTime) {
        long timeSpent = endTime - startTime;

        DocumentReference docRef = db.collection("timeSpentTracker").document(documentPath);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    Long temp = document.getLong("timeSpent");

                    long updatedTimeSpent;
                    if (temp != null) {
                        updatedTimeSpent = timeSpent + temp;
                    } else {
                        updatedTimeSpent = timeSpent;
                    }
                    Log.d("TimeTracker", "Time spent in app: " + updatedTimeSpent + " milliseconds for document: " + documentPath);

                    Map<String, Object> timeData = new HashMap<>();
                    timeData.put("timeSpent", updatedTimeSpent);

                    docRef.set(timeData, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TimeTracker", "Time spent successfully written for document: " + documentPath);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("TimeTracker", "Error writing time spent for document: " + documentPath, e);
                                }
                            });
                }
            }
        });
    }
}
