package sg.edu.np.mad.fitnessultimate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ExerciseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exercise);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        ExerciseInfo exercise = intent.getParcelableExtra("exercise", ExerciseInfo.class);

        TextView exerciseTitle = findViewById(R.id.exerciseTitle);
        TextView exerciseDesc = findViewById(R.id.exerciseDescription);
        TextView exerciseMuscleGroup = findViewById(R.id.exerciseMuscleGroup);
        TextView exerciseDifficulty = findViewById(R.id.exerciseDifficulty);
        Button backBtn = findViewById(R.id.backBtn);

        assert exercise != null;
        exerciseTitle.setText(exercise.getName());
        exerciseDesc.setText(exercise.getDescription());
        exerciseMuscleGroup.setText(String.format("Muscle Group: %s", exercise.getMuscleGroup()));
        exerciseDifficulty.setText(String.format("Difficulty: %s", exercise.getDifficulty()));
        backBtn.setOnClickListener(v -> finish());
    }
}