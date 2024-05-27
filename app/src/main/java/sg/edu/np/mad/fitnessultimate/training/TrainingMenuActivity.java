package sg.edu.np.mad.fitnessultimate.training;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.training.exercises.ExerciseCatalogueActivity;
import sg.edu.np.mad.fitnessultimate.training.helpers.GlobalExerciseData;
import sg.edu.np.mad.fitnessultimate.training.helpers.JsonUtils;
import sg.edu.np.mad.fitnessultimate.training.workouts.WorkoutsCatalogueActivity;

public class TrainingMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_training_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // populate singleton class with exercise and workout list
        GlobalExerciseData.getInstance().setExerciseList(JsonUtils.loadExercises(this));
        GlobalExerciseData.getInstance().setWorkoutList(JsonUtils.loadWorkouts(this));

        // locate buttons
        Button followAlongWorkouts = findViewById(R.id.followAlongWorkouts);
        Button exercises = findViewById(R.id.exercises);

        // set button click listeners
        followAlongWorkouts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingMenuActivity.this, WorkoutsCatalogueActivity.class);

                // logging for debugging purposes
                Log.i(this.getClass().getSimpleName(),
                        String.format("[%s]: redirecting to activity %s",
                        TrainingMenuActivity.class.getSimpleName(),
                        WorkoutsCatalogueActivity.class.getSimpleName()));
                startActivity(intent);
            }
        });

        exercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingMenuActivity.this, ExerciseCatalogueActivity.class);
                Log.i(this.getClass().getSimpleName(),
                        String.format("[%s]: redirecting to activity %s",
                                TrainingMenuActivity.class.getSimpleName(),
                                ExerciseCatalogueActivity.class.getSimpleName()));
                startActivity(intent);
            }
        });
    }
}