package sg.edu.np.mad.fitnessultimate.calendarPage;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

import sg.edu.np.mad.fitnessultimate.R;

class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder>
{

    private final ArrayList<HistoryClass> workoutsList;
    private final OnItemListener2 onItemListener2;

    public HistoryAdapter(ArrayList<HistoryClass> workoutsList, OnItemListener2 onItemListener2)
    {
        this.workoutsList = workoutsList;
        this.onItemListener2 = onItemListener2;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.history_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        view.setLayoutParams(layoutParams);
        return new HistoryViewHolder(view, onItemListener2);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryClass historyClass = workoutsList.get(position);
        holder.setHistoryClass(historyClass);

        LocalDate date =  historyClass.dayText;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String day = date.format(formatter) + "\n" + date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());;
        holder.dateLabel.setText(day);
        holder.workoutName.setText(String.valueOf(historyClass.workout.getName()));
        holder.workoutDescription.setText(String.valueOf(historyClass.workout.getDescription()));

        // Ensure the text does not go to the next line
        holder.workoutDescription.setSingleLine(true);
        holder.workoutDescription.setEllipsize(TextUtils.TruncateAt.END);
        holder.workoutDescription.setMaxLines(1);
        holder.workoutDescription.setHorizontallyScrolling(true);

        Log.d("HistoryAdapter", day);
    }


    @Override
    public int getItemCount()
    {
        return workoutsList.size();
    }

    public interface  OnItemListener2
    {
        void onItemClick2(int position, HistoryClass historyClass);
    }
}