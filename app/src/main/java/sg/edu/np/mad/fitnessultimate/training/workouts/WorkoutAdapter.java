package sg.edu.np.mad.fitnessultimate.training.workouts;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.fitnessultimate.R;


public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutViewHolder> {
    private List<Workout> workoutList;

    public WorkoutAdapter(List<Workout> workoutList) {
        this.workoutList = workoutList;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workout_item, parent, false);
        return new WorkoutViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        holder.workoutName.setText(workout.getName());
        holder.workoutName.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), WorkoutActivity.class);
            intent.putExtra("workout", workout);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }
}