package sg.edu.np.mad.fitnessultimate;

import java.time.LocalDate;

public class DayModel {
    public String dayText;
    public boolean isCurrentMonth;
    public LocalDate fullDate;

    public DayModel(String dayText, boolean isCurrentMonth, LocalDate fullDate) {
        this.dayText = dayText;
        this.isCurrentMonth = isCurrentMonth;
        this.fullDate = fullDate;
    }
}
