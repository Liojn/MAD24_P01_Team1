package sg.edu.np.mad.fitnessultimate.waterTracker.water;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sg.edu.np.mad.fitnessultimate.R;

public class IntakeHistoryAdapter extends RecyclerView.Adapter<IntakeHistoryAdapter.IntakeHistoryViewHolder> {
    private List<IntakeHistory> intakeHistoryList;

    public IntakeHistoryAdapter(List<IntakeHistory> intakeHistoryList) {
        this.intakeHistoryList = intakeHistoryList;
    }

    @NonNull
    @Override
    public IntakeHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.intake_history_item, parent, false);
        return new IntakeHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IntakeHistoryViewHolder holder, int position) {
        IntakeHistory history = intakeHistoryList.get(position);
        holder.intakeAmountTime.setText(history.getTime());
        holder.incrementTextView.setText("+ " + history.getIncrement());
    }

    @Override
    public int getItemCount() {
        return intakeHistoryList.size();
    }

    static class IntakeHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView intakeAmountTime;
        TextView incrementTextView;

        IntakeHistoryViewHolder(View itemView) {
            super(itemView);
            intakeAmountTime = itemView.findViewById(R.id.textViewTime);
            incrementTextView = itemView.findViewById(R.id.textViewVolume);
        }
    }

    public void updateData(List<IntakeHistory> newList) {
        intakeHistoryList.clear();
        intakeHistoryList.addAll(newList);
        notifyDataSetChanged();
    }

}
