<<<<<<<< HEAD:app/src/main/java/sg/edu/np/mad/fitnessultimate/calendar/DayModel.java
package sg.edu.np.mad.fitnessultimate.calendar;
========
package sg.edu.np.mad.fitnessultimate.calendarPage;
>>>>>>>> c2bf8b5b669f4443abcb052a6ca3a5677416c8e9:app/src/main/java/sg/edu/np/mad/fitnessultimate/calendarPage/DayModel.java

import java.time.LocalDate;

import sg.edu.np.mad.fitnessultimate.workoutPage.training.workouts.Workout;

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
