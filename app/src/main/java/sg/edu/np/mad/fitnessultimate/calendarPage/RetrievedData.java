package sg.edu.np.mad.fitnessultimate.calendarPage;

import sg.edu.np.mad.fitnessultimate.training.workouts.Workout;

public class RetrievedData {
    public Long timeSpent;
    public Workout workout;

    public RetrievedData(Long timeSpent, Workout workout){
        this.timeSpent = timeSpent;
        this.workout = workout;
    }
}
