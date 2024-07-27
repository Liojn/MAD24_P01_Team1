package sg.edu.np.mad.fitnessultimate.training.workouts;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.training.helpers.GlobalExerciseData;

public class WorkoutsCatalogueActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workouts_catalogue);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button backBtn = findViewById(R.id.backBtn);
        RecyclerView exerciseRecyclerView = findViewById(R.id.workoutsRecyclerView);
        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Workout> workoutsList = GlobalExerciseData.getInstance().getWorkoutList();

        backBtn.setOnClickListener(v -> finish());
        WorkoutAdapter workoutAdapter = new WorkoutAdapter(workoutsList);
        exerciseRecyclerView.setAdapter(workoutAdapter);
    }
}