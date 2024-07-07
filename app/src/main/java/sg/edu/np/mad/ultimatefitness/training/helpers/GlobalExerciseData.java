package sg.edu.np.mad.ultimatefitness.training.helpers;

import java.util.List;
import java.util.ArrayList;

import sg.edu.np.mad.ultimatefitness.training.exercises.ExerciseInfo;
import sg.edu.np.mad.ultimatefitness.training.workouts.Workout;

/*
 * singleton class to store list of exercises
 * used to store the list of exercises loaded from json file
 */

public class GlobalExerciseData {
    private static GlobalExerciseData inst;
    private List<ExerciseInfo> exerciseList;
    private List<Workout> workoutList;

    private GlobalExerciseData() {
        exerciseList = new ArrayList<>();
        workoutList = new ArrayList<>();
    }

    // prevent multiple instances of this object from being created at the same time
    // by returning existing instance if it exists already

    //synchronized to prevent race conditions when writing data
    public static synchronized GlobalExerciseData getInstance() {
        if (inst == null) {
            inst = new GlobalExerciseData();
        }
        return inst;
    }

    public List<ExerciseInfo> getExerciseList() {
        return exerciseList;
    }

    public List<Workout> getWorkoutList() {
        return workoutList;
    }

    public void setExerciseList(List<ExerciseInfo> exerciseList) {
        this.exerciseList = exerciseList;
    }

    public void setWorkoutList(List<Workout> workoutList) {
        this.workoutList = workoutList;
    }
}