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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class TrainingMenu extends AppCompatActivity {

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
                Intent intent = new Intent(TrainingMenu.this, FollowAlongWorkouts.class);
                Log.i(this.getClass().getSimpleName(),
                        String.format("[%s]: redirecting to activity %s",
                        TrainingMenu.class.getSimpleName(),
                        FollowAlongWorkouts.class.getSimpleName()));
                startActivity(intent);
            }
        });

        exercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingMenu.this, ExerciseCatalogue.class);
                Log.i(this.getClass().getSimpleName(),
                        String.format("[%s]: redirecting to activity %s",
                                TrainingMenu.class.getSimpleName(),
                                ExerciseCatalogue.class.getSimpleName()));
                startActivity(intent);
            }
        });
    }
}