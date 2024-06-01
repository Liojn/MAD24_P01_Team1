package sg.edu.np.mad.fitnessultimate.calendarPage;

import sg.edu.np.mad.fitnessultimate.training.workouts.Workout;

public class WorkoutPlan {
    public Long timeSpent;
    public Workout workout;

    public WorkoutPlan(Long timeSpent, Workout workout){
        this.timeSpent = timeSpent;
        this.workout = workout;
    }
}
