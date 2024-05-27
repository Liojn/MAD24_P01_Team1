package sg.edu.np.mad.fitnessultimate;

import java.util.ArrayList;
import java.util.List;

public class Workout {
    private String name;
    private String description;
    private int breakTimeInMinutes;
    private int estimatedTimeInMinutes;
    private List<Exercise> exercises;
    private List<ExerciseInfo> exerciseInfoList = new ArrayList<>();

    public Workout(String name, String description, int breakTimeInMinutes, int estimatedTimeInMinutes, List<Exercise> exercises) {
        this.name = name;
        this.description = description;
        this.breakTimeInMinutes = breakTimeInMinutes;
        this.estimatedTimeInMinutes = estimatedTimeInMinutes;
        this.exercises = exercises;

        // populate exercise info list at initialisation to avoid ensure smooth lookup
        for (Exercise exercise : exercises) {
            exerciseInfoList.add(exercise.getExerciseInfo());
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getBreakTimeInMinutes() {
        return breakTimeInMinutes;
    }

    public int getEstimatedTimeInMinutes() {
        return estimatedTimeInMinutes;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    // inner class to store exercise info relative to workout
    public static class Exercise {
        private String name;
        private int count;
        private int sets;
        private ExerciseInfo exerciseInfo;

        public Exercise(String name, int count, int sets) {
            this.name = name;
            this.count = count;
            this.sets = sets;
            this.exerciseInfo = GlobalExerciseData.getInstance().getExerciseList().stream()
                    .filter(exercise -> exercise.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public int getSets() {
            return sets;
        }

        public ExerciseInfo getExerciseInfo() {
            return exerciseInfo;
        }
    }
}
