
package sg.edu.np.mad.fitnessultimate.calendarPage;

import java.time.LocalDate;

import sg.edu.np.mad.fitnessultimate.training.workouts.Workout;

public class DayModel {
    public String dayText;
    public boolean isCurrentMonth;
    public LocalDate fullDate;
    public long timeSpent;
    public Workout workout;

    public DayModel(String dayText, boolean isCurrentMonth, LocalDate fullDate, long timeSpent, Workout workout) {
        this.dayText = dayText;
        this.isCurrentMonth = isCurrentMonth;
        this.fullDate = fullDate;
        this.timeSpent = timeSpent;
        this.workout = workout;
    }
}
