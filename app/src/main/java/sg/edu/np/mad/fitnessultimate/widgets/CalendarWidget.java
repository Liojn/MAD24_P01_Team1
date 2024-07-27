package sg.edu.np.mad.fitnessultimate.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.calendarPage.DayModel;
import sg.edu.np.mad.fitnessultimate.training.helpers.GlobalExerciseData;
import sg.edu.np.mad.fitnessultimate.training.helpers.JsonUtils;

public class CalendarWidget extends AppWidgetProvider {

    public static final String ACTION_NEXT_MONTH = "sg.edu.np.mad.fitnessultimate.widgets.ACTION_NEXT_MONTH";
    public static final String ACTION_PREVIOUS_MONTH = "sg.edu.np.mad.fitnessultimate.widgets.ACTION_PREVIOUS_MONTH";
    public static int sizeOfWidget;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bundle options, int onMonth) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.calendar_widget);

        // Check widget size and adjust attributes
        if (options != null) {
            int maxWidgetHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
            sizeOfWidget = (int) Math.round((double) maxWidgetHeight / 116);

            if (sizeOfWidget == 4){
                views.setViewPadding(R.id.calendarWidgetTop, 0, 5, 0, 5);
            } else {
                views.setViewPadding(R.id.calendarWidgetTop, 0, 10, 0, 10);
            }
        }

        // Set up the intent that starts the CalendarWidgetService, which will
        // provide the views for this collection.
        Intent intent = new Intent(context, CalendarWidgetService.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("key", onMonth); // Ensure unique extras
        intent.putExtra("sizeOfWidget", sizeOfWidget == 0 ? 4 : sizeOfWidget);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // Ensure the intent is unique
        views.setRemoteAdapter(R.id.calendar_widget_gridView, intent);

        CalendarDataRepository.getInstance().setDaysReadyListener(new CalendarDataRepository.DaysReadyListener() {
            @Override
            public void onDaysReady() {
                ArrayList<DayModel> daysInMonth = CalendarDataRepository.getInstance().getDaysInMonth();

                // Set Month Text
                views.setTextViewText(R.id.monthYearTV, daysInMonth.get(15).fullDate.format(DateTimeFormatter.ofPattern("MMMM")));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });

        // Set up the PendingIntent for the "Previous Month" button click
        Intent prevButtonIntent = new Intent(context, CalendarWidget.class);
        prevButtonIntent.setAction(ACTION_PREVIOUS_MONTH);
        prevButtonIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        prevButtonIntent.putExtra("key", onMonth);
        prevButtonIntent.putExtra("option", options);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(context, 0, prevButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.previousMonthButton, prevPendingIntent);

        // Set up the PendingIntent for the "Next Month" button click
        Intent buttonIntent = new Intent(context, CalendarWidget.class);
        buttonIntent.setAction(ACTION_NEXT_MONTH);
        buttonIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        buttonIntent.putExtra("key", onMonth);
        buttonIntent.putExtra("option", options);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.nextMonthButton, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Bundle options = intent.getBundleExtra("options");
        if (Objects.equals(intent.getAction(), ACTION_NEXT_MONTH)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int onMonth = intent.getIntExtra("key", 0);
            onMonth += 1; // Increment the month
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, options, onMonth);
        } else if (Objects.equals(intent.getAction(), ACTION_PREVIOUS_MONTH)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int onMonth = intent.getIntExtra("key", 0);
            onMonth -= 1; // Decrement the month
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, options, onMonth);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        updateAppWidget(context, appWidgetManager, appWidgetId, newOptions, 0);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        GlobalExerciseData.getInstance().setWorkoutList(JsonUtils.loadWorkouts(context));
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null, 0);
        }
    }
}