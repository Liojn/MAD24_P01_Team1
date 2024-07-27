package sg.edu.np.mad.fitnessultimate.calendarPage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.training.workouts.Workout;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<DayModel> daysOfMonth;
    private final OnItemListener onItemListener;
    private final Context context;

    public CalendarAdapter(Context context, ArrayList<DayModel> daysOfMonth, OnItemListener onItemListener)
    {
        this.context = context;
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

        // Background size
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.dayOfMonthBg.getLayoutParams();
        params.height = 102;
        int marginDp = 10;
        int marginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp, holder.itemView.getResources().getDisplayMetrics());
        params.setMargins(marginPx, marginPx, marginPx, marginPx);
        holder.dayOfMonthBg.setLayoutParams(params);
        holder.calenderCellOl.setLayoutParams(params);

        // Date color
        if (!dayModel.isCurrentMonth) {
            holder.dayOfMonth.setTextColor(Color.rgb(190, 190, 190));
        }

        // Set background color based on time spent
        long timeSpent = dayModel.timeSpent/1000;
        GradientDrawable background = (GradientDrawable) holder.dayOfMonthBg.getBackground();
        background.setColor(MiscCalendar.getColorForTimeSpent(timeSpent));
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