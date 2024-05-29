package sg.edu.np.mad.fitnessultimate.training.workouts;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.fitnessultimate.R;


public class RoutineAdapter extends RecyclerView.Adapter<RoutineViewHolder> {
    private List<Workout.Exercise> exercises;

    public RoutineAdapter(List<Workout.Exercise> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.routine_item, parent, false);
        return new RoutineViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Workout.Exercise exercise = exercises.get(position);
        holder.exerciseTitle.setText(exercise.getName());
        holder.exerciseReps.setText(String.format("%s reps", exercise.getCount()));
        holder.exerciseSets.setText(String.format("%s sets", exercise.getSets()));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }
}