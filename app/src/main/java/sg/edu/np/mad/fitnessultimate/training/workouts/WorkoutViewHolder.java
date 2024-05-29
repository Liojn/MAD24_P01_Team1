package sg.edu.np.mad.fitnessultimate.training.workouts;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.fitnessultimate.R;

public class WorkoutViewHolder extends RecyclerView.ViewHolder {
    TextView workoutName;

    public WorkoutViewHolder(@NonNull View itemView) {
        super(itemView);
        workoutName = itemView.findViewById(R.id.workoutButton);
    }
}
