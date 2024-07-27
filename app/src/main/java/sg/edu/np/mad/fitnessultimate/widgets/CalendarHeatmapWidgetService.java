package sg.edu.np.mad.fitnessultimate.widgets;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class CalendarHeatmapWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CalendarHeatmapWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}