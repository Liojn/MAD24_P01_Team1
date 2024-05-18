package sg.edu.np.mad.fitnessultimate;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseViewHolder> {
    private List<ExerciseInfo> exerciseList;

    public ExerciseAdapter(List<ExerciseInfo> exerciseList) {
        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exercise_item, parent, false);
        return new ExerciseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseInfo exercise = exerciseList.get(position);
        holder.exerciseName.setText(exercise.getName());
        holder.exerciseName.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ExerciseActivity.class);
            intent.putExtra("exercise", exercise);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        int size = exerciseList.size();
        Log.i(this.getClass().getSimpleName(), String.format("[%s]: item count: %d", this.getClass().getSimpleName(), size));
        return size;
    }
}