package sg.edu.np.mad.fitnessultimate;

import java.util.List;
import java.util.ArrayList;

/*
    * singleton class to store list of exercises
    * used to store the list of exercises loaded from json file
 */

public class GlobalExerciseList {
    private static GlobalExerciseList inst;
    private List<ExerciseInfo> exerciseList;

    private GlobalExerciseList() {
        exerciseList = new ArrayList<>();
    }

    public static synchronized GlobalExerciseList getInstance() {
        if (inst == null) {
            inst = new GlobalExerciseList();
        }
        return inst;
    }

    public List<ExerciseInfo> getExerciseList() {
        return exerciseList;
    }

    public void setExerciseList(List<ExerciseInfo> exerciseList) {
        this.exerciseList = exerciseList;
    }
}