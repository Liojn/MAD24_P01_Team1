package sg.edu.np.mad.fitnessultimate.training.exercises;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.fitnessultimate.R;

public class ExerciseViewHolder extends RecyclerView.ViewHolder {
    TextView exerciseName;

    public ExerciseViewHolder(@NonNull View itemView) {
        super(itemView);
        exerciseName = itemView.findViewById(R.id.exerciseButton);
    }
}
