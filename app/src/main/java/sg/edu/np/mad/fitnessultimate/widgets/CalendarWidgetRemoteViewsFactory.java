package sg.edu.np.mad.fitnessultimate.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.calendarPage.DayModel;
import sg.edu.np.mad.fitnessultimate.calendarPage.MiscCalendar;
import sg.edu.np.mad.fitnessultimate.calendarPage.RetrievedData;
import sg.edu.np.mad.fitnessultimate.training.workouts.Workout;

public class CalendarWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context mContext;
    private final Intent mIntent;
    private ArrayList<DayModel> mDaysInMonth = new ArrayList<>(); // Initialize to an empty list
    private LocalDate currentDate;
    private int onMonth;
    private int sizeOfWidget;
    private String month;

    public CalendarWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
    }

    @Override
    public void onCreate() {
        // Retrieve the bundle
        Bundle bundle = mIntent.getExtras();
        onMonth = bundle.getInt("key");
        sizeOfWidget = bundle.getInt("sizeOfWidget");

        // Load the data
        currentDate = LocalDate.now();

        MiscCalendar.getTimeSpentForDate(new MiscCalendar.FirestoreCallback() {
            public void onCallback(Map<LocalDate, RetrievedData> dateDataMap) {
                mDaysInMonth = MiscCalendar.createDaysInMonthArray(currentDate.plusMonths(onMonth), dateDataMap);
                month = mDaysInMonth.get(15).fullDate.format(DateTimeFormatter.ofPattern("MMMM"));
                notifyAppWidgetViewDataChanged(); // Notify that the data has changed
            }
        });
    }

    @Override
    public void onDataSetChanged() {
        // change
    }

    @Override
    public void onDestroy() {
        // Clean up any resources
    }

    @Override
    public int getCount() {
        if (!mDaysInMonth.isEmpty()) {
            if (Objects.equals(mDaysInMonth.get(15).fullDate.format(DateTimeFormatter.ofPattern("MMMM")), month)){
                CalendarDataRepository.getInstance().setDaysInMonth(mDaysInMonth);
            }
        }
        return mDaysInMonth != null ? mDaysInMonth.size() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (mDaysInMonth == null || mDaysInMonth.isEmpty()) {
            return null; // Return null if data is not available
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.calendar_cell);

        Log.i("herehere", String.valueOf(sizeOfWidget));
        // Set Height
        if (sizeOfWidget == 4) {
            if (mDaysInMonth.size() > 40) {
                views.setViewLayoutHeight(R.id.calenderCell, 55, TypedValue.COMPLEX_UNIT_DIP);
            } else {
                views.setViewLayoutHeight(R.id.calenderCell, 65, TypedValue.COMPLEX_UNIT_DIP);
            }
        } else {
            if (mDaysInMonth.size() > 40) {
                views.setViewLayoutHeight(R.id.calenderCell, 75, TypedValue.COMPLEX_UNIT_DIP);
            } else {
                views.setViewLayoutHeight(R.id.calenderCell, 85, TypedValue.COMPLEX_UNIT_DIP);
            }
        }


        // Set your views here based on mDaysInMonth.get(position)
        // Remove backgrounds
        views.setViewLayoutHeight(R.id.calenderCellOl, 40, TypedValue.COMPLEX_UNIT_DIP);
        views.setViewLayoutWidth(R.id.calenderCellOl, 40, TypedValue.COMPLEX_UNIT_DIP);
        views.setViewLayoutHeight(R.id.calenderCellBg, 40, TypedValue.COMPLEX_UNIT_DIP);
        views.setViewLayoutWidth(R.id.calenderCellBg, 40, TypedValue.COMPLEX_UNIT_DIP);
        views.setInt(R.id.calenderCellBg, "setBackgroundColor", Color.TRANSPARENT);
        views.setImageViewResource(R.id.calenderCellBg, R.drawable.circle);
        views.setViewVisibility(R.id.smallMarker, View.GONE);

        //set cell height

        // Set date text
        views.setTextViewText(R.id.cellDayText, mDaysInMonth.get(position).dayText);
        // Date color
        if (!mDaysInMonth.get(position).isCurrentMonth) {
            views.setTextColor(R.id.cellDayText, Color.rgb(190, 190, 190));
        }
        // Set if today
        if (String.valueOf(mDaysInMonth.get(position).fullDate).equals(String.valueOf(LocalDate.now()))) {
            views.setViewVisibility(R.id.calenderCellOl, View.VISIBLE);
        }
        // Check Workout
        if (mDaysInMonth.get(position).workout != null){
            views.setViewVisibility(R.id.cellDayText, View.GONE);
            views.setViewVisibility(R.id.smallTick, View.VISIBLE);
        }
        // Get Color
        DayModel dayModel = mDaysInMonth.get(position);
        long timeSpent = dayModel.timeSpent/1000;
        int newColor = MiscCalendar.getColorForTimeSpent(timeSpent);
        if (newColor == Color.TRANSPARENT){
            newColor = Color.parseColor("#E1E4F6");
        }
        views.setInt(R.id.calenderCellBg, "setColorFilter", newColor);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void notifyAppWidgetViewDataChanged() {
        // Notify the app widget manager that the data has changed
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        ComponentName componentName = new ComponentName(mContext, CalendarWidget.class);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.calendar_widget_gridView);
    }
}
