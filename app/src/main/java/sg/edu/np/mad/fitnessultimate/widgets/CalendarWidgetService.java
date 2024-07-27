package sg.edu.np.mad.fitnessultimate.widgets;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class CalendarWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CalendarWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
