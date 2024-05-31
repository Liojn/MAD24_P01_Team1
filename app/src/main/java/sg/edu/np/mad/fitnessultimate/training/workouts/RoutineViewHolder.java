package sg.edu.np.mad.fitnessultimate.training.workouts;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import sg.edu.np.mad.fitnessultimate.R;

public class RoutineViewHolder extends RecyclerView.ViewHolder {
    TextView exerciseTitle;
    TextView exerciseReps;
    TextView exerciseDescription;
    TextView exerciseSets;

    public RoutineViewHolder(@NonNull View itemView) {
        super(itemView);
        exerciseTitle = itemView.findViewById(R.id.exerciseTitle);
        exerciseReps = itemView.findViewById(R.id.exerciseReps);
        exerciseDescription = itemView.findViewById(R.id.exerciseDescription);
        exerciseSets = itemView.findViewById(R.id.exerciseSets);
    }
}
