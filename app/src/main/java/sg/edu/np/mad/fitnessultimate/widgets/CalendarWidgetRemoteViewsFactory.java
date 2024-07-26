package sg.edu.np.mad.fitnessultimate.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.calendarPage.CalendarActivity;
import sg.edu.np.mad.fitnessultimate.calendarPage.DayModel;
import sg.edu.np.mad.fitnessultimate.calendarPage.MiscCalendar;
import sg.edu.np.mad.fitnessultimate.calendarPage.RetrievedData;
import sg.edu.np.mad.fitnessultimate.training.workouts.Workout;

public class CalendarWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context mContext;
    private final Intent mIntent;
    private ArrayList<DayModel> mDaysInMonth = new ArrayList<>(); // Initialize to an empty list
    private LocalDate currentDate;
    private LocalDate startDate;
    private int numOfCols;

    public CalendarWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
    }

    @Override
    public void onCreate() {
        // Retrieve the bundle
        Bundle bundle = mIntent.getExtras();
        numOfCols = bundle.getInt("key");

        // Load the data
        currentDate = LocalDate.now();

        MiscCalendar.getTimeSpentForDate(new MiscCalendar.FirestoreCallback() {
            public void onCallback(Map<LocalDate, RetrievedData> dateDataMap) {
                int dayOfWeek = currentDate.getDayOfWeek().getValue();
                int daysInCaln = 7 * (numOfCols - 1) + dayOfWeek;
                startDate = currentDate.minusDays(daysInCaln);
                int totalCells = 7 * numOfCols - 1;

                for (int i = 0; i <= totalCells; i++) {
                    int test = (int) (Math.floor((double) i / numOfCols) + (i % numOfCols * 7));
                    LocalDate onDate = startDate.plusDays(test);

                    Long timeSpent = 0L;
                    Workout workout = null;
                    if (dateDataMap.containsKey(onDate)) {
                        RetrievedData retrievedData = dateDataMap.get(onDate);
                        if (retrievedData.timeSpent == null) {
                            timeSpent = 0L;
                        } else {
                            timeSpent = retrievedData.timeSpent;
                        }
                        workout = retrievedData.workout;
                    }

                    Boolean shown = true;
                    if (onDate.isAfter(currentDate)) {
                        shown = false;
                    }

                    mDaysInMonth.add(new DayModel(String.valueOf(onDate.getDayOfMonth()), shown, onDate, timeSpent, workout));
                }

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
            CalendarDataRepository.getInstance().setDaysInMonth(mDaysInMonth); // Store data in singleton
        }
        return mDaysInMonth != null ? mDaysInMonth.size() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (mDaysInMonth == null || mDaysInMonth.isEmpty()) {
            return null; // Return null if data is not available
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.calendar_widget_cell);

        // Set your views here based on mDaysInMonth.get(position)
        // Set Grid Cell Visibility
        if (!mDaysInMonth.get(position).isCurrentMonth) {
            views.setViewVisibility(R.id.widgetCellBg, View.GONE);
        }
        // set first day of month
        if (Objects.equals(mDaysInMonth.get(position).dayText, "1")){
            views.setViewVisibility(R.id.calenderCellOl, View.VISIBLE);
        }
        // Check Workout
        if (mDaysInMonth.get(position).workout != null){
            views.setViewVisibility(R.id.smallTick, View.VISIBLE);
        }
        // Get Color
        DayModel dayModel = mDaysInMonth.get(position);
        long timeSpent = dayModel.timeSpent/1000;
        int newColor = MiscCalendar.getColorForTimeSpent(timeSpent);
        if (newColor == Color.TRANSPARENT){
            newColor = Color.parseColor("#1f252f");
        }
        // Set Color
        views.setInt(R.id.widgetCellBg, "setColorFilter", newColor);

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