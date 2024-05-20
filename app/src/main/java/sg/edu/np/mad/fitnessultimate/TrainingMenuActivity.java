package sg.edu.np.mad.fitnessultimate;

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

import java.util.List;

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

        // note to self: objects in java are passed by reference (i.e &object)
        List<ExerciseInfo> exerciseList = JsonUtils.loadExercises(this);

        Button followAlongWorkouts = findViewById(R.id.followAlongWorkouts);
        Button exercises = findViewById(R.id.exercises);

        followAlongWorkouts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingMenuActivity.this, FollowAlongWorkoutsActivity.class);
                Log.i(this.getClass().getSimpleName(),
                        String.format("[%s]: redirecting to activity %s",
                        TrainingMenuActivity.class.getSimpleName(),
                        FollowAlongWorkoutsActivity.class.getSimpleName()));
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