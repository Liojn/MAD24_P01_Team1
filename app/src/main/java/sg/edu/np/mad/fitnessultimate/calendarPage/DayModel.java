package sg.edu.np.mad.fitnessultimate.calendarPage;

import java.time.LocalDate;

public class DayModel {
    public String dayText;
    public boolean isCurrentMonth;
    public LocalDate fullDate;
    public long timeSpent;

    public DayModel(String dayText, boolean isCurrentMonth, LocalDate fullDate, long timeSpent) {
        this.dayText = dayText;
        this.isCurrentMonth = isCurrentMonth;
        this.fullDate = fullDate;
        this.timeSpent = timeSpent;
    }
}
