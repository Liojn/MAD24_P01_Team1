package sg.edu.np.mad.ultimatefitness.calendarPage;

import java.time.LocalDate;

import sg.edu.np.mad.ultimatefitness.training.workouts.Workout;

public class HistoryClass {
    public LocalDate dayText;
    public Workout workout;

    public HistoryClass(LocalDate dayText, Workout workout) {
        this.dayText = dayText;
        this.workout = workout;
    }
}
