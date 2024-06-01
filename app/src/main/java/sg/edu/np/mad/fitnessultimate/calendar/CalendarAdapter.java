package sg.edu.np.mad.fitnessultimate.calendar;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.mad.fitnessultimate.R;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<DayModel> daysOfMonth;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<DayModel> daysOfMonth, OnItemListener onItemListener)
    {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        // Calculate the height dynamically based on the parent height divided by the number of rows
        int totalItems = daysOfMonth.size();
        int numRows = (totalItems <= 35) ? 5 : 6;
        layoutParams.height = (int) (parent.getHeight() / (float) numRows);

        view.setLayoutParams(layoutParams);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        DayModel dayModel = daysOfMonth.get(position);
        holder.setDayModel(dayModel);
        holder.dayOfMonth.setText(dayModel.dayText);
        if (!dayModel.isCurrentMonth){
            holder.dayOfMonth.setTextColor(Color.rgb(190,190,190));
        }
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, DayModel dayModel);
    }
}