<<<<<<<< HEAD:app/src/main/java/sg/edu/np/mad/fitnessultimate/calendar/CalendarViewHolder.java
package sg.edu.np.mad.fitnessultimate.calendar;
========
package sg.edu.np.mad.fitnessultimate.calendarPage;
>>>>>>>> c2bf8b5b669f4443abcb052a6ca3a5677416c8e9:app/src/main/java/sg/edu/np/mad/fitnessultimate/calendarPage/CalendarViewHolder.java

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.fitnessultimate.R;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public final TextView dayOfMonth;
    public final View dayOfMonthBg;
    public final View calenderCellOl;
    public final View smallMarker;
    private final CalendarAdapter.OnItemListener onItemListener;
    private DayModel dayModel;
    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener)
    {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.cellDayText);
        dayOfMonthBg = itemView.findViewById(R.id.calenderCellBg);
        calenderCellOl = itemView.findViewById(R.id.calenderCellOl);
        smallMarker = itemView.findViewById(R.id.smallMarker);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    public void setDayModel(DayModel dayModel) {
        this.dayModel = dayModel;
    }

    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(), dayModel);
    }
}
