package sg.edu.np.mad.fitnessultimate.training.workouts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.training.exercises.ExerciseAdapter;
import sg.edu.np.mad.fitnessultimate.training.exercises.ExerciseInfo;
import sg.edu.np.mad.fitnessultimate.training.helpers.GlobalExerciseData;

public class WorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        Workout workout = intent.getParcelableExtra("workout", Workout.class);

        List<ExerciseInfo> exerciseInfoList = new ArrayList<>();

        for (Workout.Exercise exercise : workout.getExercises()) {
            ExerciseInfo info = GlobalExerciseData.getInstance().getExerciseList().stream()
                    .filter(e -> e.getName().equals(exercise.getName()))
                    .findFirst()
                    .orElse(null);

            exerciseInfoList.add(info);
        }

        Button backBtn = findViewById(R.id.backBtn);
        TextView workoutTitle = findViewById(R.id.workoutTitle);
        TextView workoutDescription = findViewById(R.id.workoutDescription);
        TextView workoutBreak = findViewById(R.id.workoutBreakTime);
        TextView workoutDuration = findViewById(R.id.workoutTimeTaken);

        workoutTitle.setText(workout.getName());
        workoutDescription.setText(workout.getDescription());
        workoutBreak.setText(String.format("Break time between Sets: %s minute(s)", workout.getBreakTimeInMinutes()));
        workoutDuration.setText(String.format("Duration: %s minute(s)", workout.getEstimatedTimeInMinutes()));
        backBtn.setOnClickListener(v -> finish());


        RecyclerView routineRecyclerView = findViewById(R.id.routineRecyclerView);
        RecyclerView exerciseRecyclerView = findViewById(R.id.featuredExerciseRecyclerView);

        routineRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RoutineAdapter routineAdapter = new RoutineAdapter(workout.getExercises());
        routineRecyclerView.setAdapter(routineAdapter);

        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ExerciseAdapter exerciseAdapter = new ExerciseAdapter(exerciseInfoList);
        exerciseRecyclerView.setAdapter(exerciseAdapter);
    }
}