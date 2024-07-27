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
import sg.edu.np.mad.fitnessultimate.calendarPage.CalendarActivity;
import sg.edu.np.mad.fitnessultimate.calendarPage.DayModel;
import sg.edu.np.mad.fitnessultimate.training.helpers.GlobalExerciseData;
import sg.edu.np.mad.fitnessultimate.training.helpers.JsonUtils;

public class CalendarHeatmapWidget extends AppWidgetProvider {
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bundle options) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.calendar_heatmap_widget);

        // Check widget size and adjust attributes
        float widthOfCell = (float) 139/7;
        int numOfCols = 7;
        if (options != null) {
            int minWidgetWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            int maxWidgetWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);

            int sizeOfWidget = (int) Math.round((double) maxWidgetWidth / 155);
            int innerWidgetWidth = minWidgetWidth - 33;

            // Adjust NumColumns in grid based on size
            if (sizeOfWidget == 2) {
                numOfCols = 7;
                widthOfCell = (float) innerWidgetWidth/numOfCols;
                views.setViewLayoutMargin(R.id.calendar_heatmap_widget_gridView, RemoteViews.MARGIN_START, 1.5F, TypedValue.COMPLEX_UNIT_DIP);
                views.setInt(R.id.calendar_heatmap_widget_gridView, "setNumColumns", 7);
            } else if (sizeOfWidget == 3) {
                numOfCols = 12;
                widthOfCell = (float) innerWidgetWidth/numOfCols;
                views.setViewLayoutMargin(R.id.calendar_heatmap_widget_gridView, RemoteViews.MARGIN_START, 1.5F, TypedValue.COMPLEX_UNIT_DIP);
                views.setInt(R.id.calendar_heatmap_widget_gridView, "setNumColumns", 12);
            } else if (sizeOfWidget == 4) {
                innerWidgetWidth += 1;
                numOfCols = 16;
                widthOfCell = (float) innerWidgetWidth/numOfCols;
                views.setViewLayoutMargin(R.id.calendar_heatmap_widget_gridView, RemoteViews.MARGIN_START, 5.5F, TypedValue.COMPLEX_UNIT_DIP);
                views.setInt(R.id.calendar_heatmap_widget_gridView, "setNumColumns", 16);
            }
        }

        // Set up the intent that starts the CalendarWidgetService, which will
        // provide the views for this collection.
        Intent intent = new Intent(context, CalendarHeatmapWidgetService.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("key", numOfCols); // Ensure unique extras
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // Ensure the intent is unique
        views.setRemoteAdapter(R.id.calendar_heatmap_widget_gridView, intent);

        // Create an Intent to trigger the update
        Intent updateIntent = new Intent(context, CalendarActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, updateIntent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.calendar_heatmap_widget, pendingIntent);

        // Access daysInMonth data from the singleton
        int finalnumOfCols = numOfCols;
        float finalwidthOfCell = widthOfCell;
        ArrayList<Integer> ids = new ArrayList<Integer>() {{
            add(R.id.widget_month1);
            add(R.id.widget_month2);
            add(R.id.widget_month3);
            add(R.id.widget_month4);
        }};
        for (int i : ids) {
            views.setTextViewText(i, "");
            views.setViewLayoutMargin(i, RemoteViews.MARGIN_START, 0, TypedValue.COMPLEX_UNIT_DIP);
        }
        CalendarDataRepository.getInstance().setDaysReadyListener(new CalendarDataRepository.DaysReadyListener() {
            @Override
            public void onDaysReady() {
                ArrayList<DayModel> daysInMonth = CalendarDataRepository.getInstance().getDaysInMonth();

                // Set Month Text
                int widgetMonth = 0;
                for (DayModel i : daysInMonth) {
                    if (Objects.equals(i.dayText, "1")) {
                        float margin = daysInMonth.indexOf(i) % finalnumOfCols * finalwidthOfCell;
                        views.setTextViewText(ids.get(widgetMonth), i.fullDate.format(DateTimeFormatter.ofPattern("MMM")));
                        views.setViewLayoutMargin(ids.get(widgetMonth), RemoteViews.MARGIN_START, margin, TypedValue.COMPLEX_UNIT_DIP);
                        widgetMonth += 1;
                    }
                }

                // Update the widget on callback
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        updateAppWidget(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        GlobalExerciseData.getInstance().setWorkoutList(JsonUtils.loadWorkouts(context));
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null);
        }
    }
}
