package sg.edu.np.mad.fitnessultimate.widgets;

import java.util.ArrayList;
import sg.edu.np.mad.fitnessultimate.calendarPage.DayModel;

public class CalendarDataRepository {
    private static CalendarDataRepository instance;
    private ArrayList<DayModel> daysInMonth = new ArrayList<>();
    private DaysReadyListener daysReadyListener;

    public static CalendarDataRepository getInstance() {
        if (instance == null) {
            instance = new CalendarDataRepository();
        }
        return instance;
    }

    public ArrayList<DayModel> getDaysInMonth() {
        return daysInMonth;
    }

    public void setDaysInMonth(ArrayList<DayModel> days) {
        this.daysInMonth = days;
        if (daysReadyListener != null) {
            daysReadyListener.onDaysReady();
        }
    }

    public void setDaysReadyListener(DaysReadyListener listener) {
        this.daysReadyListener = listener;
    }

    public interface DaysReadyListener {
        void onDaysReady();
    }
}