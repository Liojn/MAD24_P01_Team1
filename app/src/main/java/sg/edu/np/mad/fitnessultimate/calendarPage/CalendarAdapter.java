package sg.edu.np.mad.fitnessultimate.calendarPage;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.workoutPage.training.workouts.Workout;

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

        layoutParams.height = (int) 180;

        view.setLayoutParams(layoutParams);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        DayModel dayModel = daysOfMonth.get(position);
        holder.setDayModel(dayModel);
        holder.dayOfMonth.setText(dayModel.dayText);

        // Date color
        if (!dayModel.isCurrentMonth) {
            holder.dayOfMonth.setTextColor(Color.rgb(190, 190, 190));
        }

        // Set background color based on time spent
        long timeSpent = dayModel.timeSpent/1000;
        GradientDrawable background = (GradientDrawable) holder.dayOfMonthBg.getBackground();
        background.setColor(getColorForTimeSpent(timeSpent));
        if (timeSpent >= 30){
            holder.dayOfMonth.setTextColor(Color.WHITE);
        }

        // Set if today
        if (String.valueOf(dayModel.fullDate).equals(String.valueOf(LocalDate.now()))) {
            holder.calenderCellOl.setVisibility(View.VISIBLE);
        }

        // Workout marker
        GradientDrawable smallMarker = (GradientDrawable) holder.smallMarker.getBackground();
        Workout workout = dayModel.workout;
        if (workout != null){
//            smallMarker.setColor(Color.rgb(45, 50, 80));
            smallMarker.setColor(Color.rgb(252, 162, 93));
        } else {
            smallMarker.setColor(Color.TRANSPARENT);
        }
    }

    private int getColorForTimeSpent(long timeSpent) {
        // Replace this logic with your desired color coding
        if (timeSpent < 30) {
            return Color.TRANSPARENT;
        } else if (timeSpent < 600) {
            return Color.rgb(159, 166, 212);
//            return Color.rgb(180, 237, 180);
        } else if (timeSpent < 1800) {
            return Color.rgb(109, 118, 181);
//            return Color.rgb(98, 227, 98);
        } else {
            return Color.rgb(73, 82, 138);
//            return Color.rgb(36, 200, 36);
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