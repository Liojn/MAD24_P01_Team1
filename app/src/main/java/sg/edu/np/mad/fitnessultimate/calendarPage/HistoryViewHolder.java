package sg.edu.np.mad.fitnessultimate.calendarPage;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.fitnessultimate.R;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public final TextView dateLabel;
    public final TextView workoutName;
    public final TextView workoutDescription;
    private final HistoryAdapter.OnItemListener2 onItemListener2;
    private HistoryClass historyClass;
    public HistoryViewHolder(@NonNull View itemView, HistoryAdapter.OnItemListener2 onItemListener2)
    {
        super(itemView);
        dateLabel = itemView.findViewById(R.id.dateLabelTV);
        workoutName = itemView.findViewById(R.id.workoutNameTV);
        workoutDescription = itemView.findViewById(R.id.workoutDescriptionTV);
        this.onItemListener2 = onItemListener2;
        itemView.setOnClickListener(this);
    }

    public void setHistoryClass(HistoryClass historyClass) {
        this.historyClass = historyClass;
    }

    @Override
    public void onClick(View view)
    {
        onItemListener2.onItemClick2(getAdapterPosition(), historyClass);
    }
}
